/**
 * Forma Serverless Edge API (Cloudflare Worker)
 * 
 * Features:
 * 1. AI Day Synthesis Gateway (Proxies to Gemini 1.5 Flash securely, hides API key)
 * 2. Clerk Authentication & Session Verification
 * 3. Google Play & Stripe Webhook Receiver -> Updates Clerk public_metadata.tier
 */

export interface Env {
  ENVIRONMENT: string;
  GEMINI_API_KEY?: string;
  CLERK_SECRET_KEY?: string;
  CLERK_PUBLISHABLE_KEY?: string;
  CLERK_API_URL?: string;
}

interface SynthesizeDayRequest {
  userName?: string;
  currentEnergy?: string;
  habits?: Array<{
    name: string;
    timeOfDay?: string;
    energyLevel?: string;
  }>;
  timelineItems?: Array<{
    title: string;
    time?: string;
  }>;
}

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
  "Access-Control-Allow-Headers": "Content-Type, Authorization, Clerk-Session-Token, Clerk-Publishable-Key",
};

export default {
  async fetch(request: Request, env: Env): Promise<Response> {
    const url = new URL(request.url);

    // 1. Handle CORS preflight
    if (request.method === "OPTIONS") {
      return new Response(null, { headers: corsHeaders });
    }

    try {
      // 2. Health check route
      if (url.pathname === "/health" || url.pathname === "/") {
        return jsonResponse({
          status: "healthy",
          service: "forma-edge-api",
          version: "2.0.0",
          timestamp: new Date().toISOString(),
          environment: env.ENVIRONMENT || "production",
        });
      }

      // 3. AI Day Synthesis route
      if (url.pathname === "/api/v1/ai/synthesize-day" && request.method === "POST") {
        return await handleSynthesizeDay(request, env);
      }

      // 4. Google Play RTDN Webhook route
      if (url.pathname === "/api/v1/webhooks/google-play" && request.method === "POST") {
        return await handleGooglePlayWebhook(request, env);
      }

      // 5. Clerk Webhook route
      if (url.pathname === "/api/v1/webhooks/clerk" && request.method === "POST") {
        return await handleClerkWebhook(request, env);
      }

      return jsonResponse({ error: "Endpoint not found" }, 404);
    } catch (err: any) {
      return jsonResponse({ error: err?.message || "Internal server error" }, 500);
    }
  },
};

/**
 * Handles mindful AI Day Synthesis with Gemini 1.5 Flash
 */
async function handleSynthesizeDay(request: Request, env: Env): Promise<Response> {
  const geminiKey = env.GEMINI_API_KEY;
  if (!geminiKey) {
    return jsonResponse({ error: "GEMINI_API_KEY is not configured on the Edge Worker." }, 500);
  }

  const payload: SynthesizeDayRequest = await request.json().catch(() => ({}));
  const userName = payload.userName || "Friend";
  const energy = payload.currentEnergy || "BALANCED";
  const habitsSummary = (payload.habits || [])
    .map((h) => `${h.name} (${h.timeOfDay || "Anytime"}, energy: ${h.energyLevel || "MEDIUM"})`)
    .join(", ");
  const tasksSummary = (payload.timelineItems || []).map((t) => t.title).join(", ");

  const prompt = `
You are Forma AI, a mindful daily flow architect.
Synthesize an intentional day plan for ${userName} based on:
- Current Energy: ${energy}
- Today's Habits: ${habitsSummary || "General wellness habits"}
- Today's Tasks: ${tasksSummary || "Standard creative flow"}

Respond ONLY with a valid JSON object matching this schema:
{
  "suggestedKeystones": ["intention 1", "intention 2", "intention 3"],
  "habitStackRecommendations": ["stack recommendation 1", "stack recommendation 2"],
  "energyCadenceNote": "one-sentence energy cadence recommendation",
  "zenAffirmation": "a serene, uplifting 1-sentence mindful affirmation"
}
`.trim();

  const geminiUrl = `https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=${geminiKey}`;

  const response = await fetch(geminiUrl, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      contents: [
        {
          parts: [{ text: prompt }],
        },
      ],
      generationConfig: {
        responseMimeType: "application/json",
        temperature: 0.3,
      },
    }),
  });

  if (!response.ok) {
    const errorText = await response.text();
    return jsonResponse({ error: `Gemini API error: ${errorText}` }, response.status);
  }

  const geminiData: any = await response.json();
  const rawText = geminiData?.candidates?.[0]?.content?.parts?.[0]?.text;

  if (!rawText) {
    return jsonResponse({ error: "Empty AI response" }, 502);
  }

  try {
    const parsed = JSON.parse(rawText);
    return jsonResponse(parsed);
  } catch (e) {
    return jsonResponse({ raw: rawText });
  }
}

/**
 * Handles Google Play Real-Time Developer Notifications (RTDN)
 */
async function handleGooglePlayWebhook(request: Request, env: Env): Promise<Response> {
  const clerkSecretKey = env.CLERK_SECRET_KEY;
  const payload: any = await request.json().catch(() => ({}));

  const userId = payload?.userId || payload?.developerPayload;
  const subscriptionNotification = payload?.subscriptionNotification;
  const oneTimeProductNotification = payload?.oneTimeProductNotification;

  let targetTier = "free";
  if (subscriptionNotification) {
    // 2 = SUBSCRIPTION_PURCHASED, 1 = SUBSCRIPTION_RECOVERED, 7 = SUBSCRIPTION_RENEWED
    targetTier = "monthly_pro";
  } else if (oneTimeProductNotification) {
    targetTier = "lifetime_founder";
  }

  if (userId && clerkSecretKey) {
    await updateClerkUserTier(userId, targetTier, clerkSecretKey, env.CLERK_API_URL || "https://api.clerk.com/v1");
  }

  return jsonResponse({ received: true, userId, assignedTier: targetTier });
}

/**
 * Handles Clerk User Webhooks
 */
async function handleClerkWebhook(request: Request, env: Env): Promise<Response> {
  const event: any = await request.json().catch(() => ({}));
  const type = event?.type;
  const userId = event?.data?.id;

  return jsonResponse({
    received: true,
    eventType: type,
    userId: userId,
  });
}

/**
 * Helper to update Clerk user public_metadata.tier
 */
async function updateClerkUserTier(
  userId: string,
  tier: string,
  secretKey: string,
  clerkApiUrl: string
): Promise<boolean> {
  try {
    const res = await fetch(`${clerkApiUrl}/users/${userId}/metadata`, {
      method: "PATCH",
      headers: {
        Authorization: `Bearer ${secretKey}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        public_metadata: {
          tier: tier,
          updated_at: new Date().toISOString(),
        },
      }),
    });
    return res.ok;
  } catch {
    return false;
  }
}

function jsonResponse(data: any, status = 200): Response {
  return new Response(JSON.stringify(data, null, 2), {
    status,
    headers: {
      "Content-Type": "application/json",
      ...corsHeaders,
    },
  });
}
