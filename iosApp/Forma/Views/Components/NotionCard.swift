import SwiftUI

public struct NotionCard<Content: View>: View {
    private let content: Content
    private let backgroundColor: Color
    private let borderColor: Color
    private let cornerRadius: CGFloat

    public init(
        backgroundColor: Color = FormaTheme.surface,
        borderColor: Color = FormaTheme.border,
        cornerRadius: CGFloat = 16,
        @ViewBuilder content: () -> Content
    ) {
        self.backgroundColor = backgroundColor
        self.borderColor = borderColor
        self.cornerRadius = cornerRadius
        self.content = content()
    }

    public var body: some View {
        content
            .padding(16)
            .background(backgroundColor)
            .cornerRadius(cornerRadius)
            .overlay(
                RoundedRectangle(cornerRadius: cornerRadius)
                    .stroke(borderColor, lineWidth: 1)
            )
    }
}
