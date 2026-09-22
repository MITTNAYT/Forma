# Forma Serverless Edge API (Cloudflare Workers)

Lean, high-speed serverless backend for **Forma: Habit Flow & Routine**.

---

## Capabilities

1. **Mindful AI Day Synthesis Proxy (`POST /api/v1/ai/synthesize-day`)**:
   - Securely proxies prompt requests to **Google Gemini 1.5 Flash**.
   - Hides `GEMINI_API_KEY` behind Cloudflare Edge infrastructure.
   - Enforces structured JSON responses.

2. **Google Play / Clerk Webhook Sync (`POST /api/v1/webhooks/google-play`)**:
   - Receives Google Play Real-Time Developer Notifications (RTDN).
   - Updates the user's Clerk `public_metadata.tier` (`monthly_pro` or `lifetime_founder`) via the Clerk Backend API.

3. **Health Check (`GET /health`)**:
   - Instant edge status & latency verification.

---

## Local Development (Testing)

From the project root:

```bash
cd backend
npm install
npm run dev
```

Your Edge API will run locally at `http://localhost:8787`.

---

## 1-Command Deployment to Cloudflare

1. Log into your Cloudflare account (if not already logged in):
```bash
npx wrangler login
```

2. Set your secrets securely:
```bash
npx wrangler secret put GEMINI_API_KEY
npx wrangler secret put CLERK_SECRET_KEY
```

3. Deploy globally:
```bash
npm run deploy
```

Your worker will be live worldwide in seconds at `https://forma-edge-api.<your-subdomain>.workers.dev`.
