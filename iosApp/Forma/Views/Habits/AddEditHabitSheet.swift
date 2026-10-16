import SwiftUI

public struct AddEditHabitSheet: View {
    @Environment(\.presentationMode) private var presentationMode
    @EnvironmentObject private var viewModel: HabitsViewModel

    let habitToEdit: Habit?
    let onSave: (Habit) -> Void

    @State private var name: String = ""
    @State private var icon: String = "target"
    @State private var colorTag: String = "#4E6542"
    @State private var timeOfDay: TimeOfDayCategory = .anytime
    @State private var energyLevel: EnergyLevel = .medium
    @State private var stackedCueText: String = ""
    @State private var repeatDays: Set<Int> = Set(1...7)

    private let availableIcons = [
        "target", "flame.fill", "sun.max.fill", "moon.stars.fill",
        "brain.head.profile", "cup.and.saucer.fill", "figure.walk",
        "book.fill", "heart.fill", "leaf.fill", "drop.fill", "sparkles"
    ]

    private let dayLabels = [
        (1, "M"), (2, "T"), (3, "W"), (4, "T"), (5, "F"), (6, "S"), (7, "S")
    ]

    public init(habitToEdit: Habit?, onSave: @escaping (Habit) -> Void) {
        self.habitToEdit = habitToEdit
        self.onSave = onSave
    }

    public var body: some View {
        NavigationView {
            ZStack {
                FormaTheme.background.ignoresSafeArea()

                ScrollView {
                    VStack(alignment: .leading, spacing: 20) {
                        // Title Input
                        VStack(alignment: .leading, spacing: 8) {
                            Text("HABIT NAME")
                                .font(.system(size: 11, weight: .bold, design: .monospaced))
                                .foregroundColor(FormaTheme.accent)

                            TextField("e.g. Read 10 Pages", text: $name)
                                .font(.system(size: 16))
                                .padding(12)
                                .background(FormaTheme.surface)
                                .cornerRadius(10)
                                .foregroundColor(FormaTheme.textPrimary)
                                .overlay(
                                    RoundedRectangle(cornerRadius: 10)
                                        .stroke(FormaTheme.border, lineWidth: 1)
                                )
                        }

                        // Habit Stacking Cue
                        VStack(alignment: .leading, spacing: 8) {
                            Text("STACKING CUE (OPTIONAL)")
                                .font(.system(size: 11, weight: .bold, design: .monospaced))
                                .foregroundColor(FormaTheme.accent)

                            TextField("e.g. After I brew morning coffee", text: $stackedCueText)
                                .font(.system(size: 14))
                                .padding(12)
                                .background(FormaTheme.surface)
                                .cornerRadius(10)
                                .foregroundColor(FormaTheme.textPrimary)
                                .overlay(
                                    RoundedRectangle(cornerRadius: 10)
                                        .stroke(FormaTheme.border, lineWidth: 1)
                                )
                        }

                        // Color Palette
                        VStack(alignment: .leading, spacing: 8) {
                            Text("COLOR HARMONY")
                                .font(.system(size: 11, weight: .bold, design: .monospaced))
                                .foregroundColor(FormaTheme.accent)

                            HStack(spacing: 12) {
                                ForEach(FormaTheme.habitPalette, id: \.self) { hex in
                                    let isSelected = colorTag == hex
                                    Circle()
                                        .fill(Color(hex: hex))
                                        .frame(width: 32, height: 32)
                                        .overlay(
                                            Circle()
                                                .stroke(isSelected ? .white : Color.clear, lineWidth: 2)
                                        )
                                        .onTapGesture {
                                            colorTag = hex
                                            ZenHaptics.selectionChanged()
                                        }
                                }
                            }
                        }

                        // Icon Selector
                        VStack(alignment: .leading, spacing: 8) {
                            Text("GLYPH ICON")
                                .font(.system(size: 11, weight: .bold, design: .monospaced))
                                .foregroundColor(FormaTheme.accent)

                            LazyVGrid(columns: Array(repeating: GridItem(.flexible()), count: 6), spacing: 10) {
                                ForEach(availableIcons, id: \.self) { iconName in
                                    let isSelected = icon == iconName
                                    ZStack {
                                        RoundedRectangle(cornerRadius: 8)
                                            .fill(isSelected ? Color(hex: colorTag) : FormaTheme.surface)
                                            .frame(height: 40)
                                            .overlay(
                                                RoundedRectangle(cornerRadius: 8)
                                                    .stroke(isSelected ? Color.white : FormaTheme.border, lineWidth: 1)
                                            )

                                        Image(systemName: iconName)
                                            .font(.system(size: 16))
                                            .foregroundColor(isSelected ? .white : FormaTheme.textSecondary)
                                    }
                                    .onTapGesture {
                                        icon = iconName
                                        ZenHaptics.selectionChanged()
                                    }
                                }
                            }
                        }

                        // Time of Day
                        VStack(alignment: .leading, spacing: 8) {
                            Text("TIME OF DAY")
                                .font(.system(size: 11, weight: .bold, design: .monospaced))
                                .foregroundColor(FormaTheme.accent)

                            HStack(spacing: 8) {
                                ForEach(TimeOfDayCategory.allCases) { category in
                                    let isSelected = timeOfDay == category
                                    Button(action: {
                                        timeOfDay = category
                                        ZenHaptics.selectionChanged()
                                    }) {
                                        Text(category.title)
                                            .font(.system(size: 12, weight: isSelected ? .semibold : .regular))
                                            .foregroundColor(isSelected ? FormaTheme.background : FormaTheme.textSecondary)
                                            .frame(maxWidth: .infinity)
                                            .padding(.vertical, 8)
                                            .background(isSelected ? FormaTheme.accent : FormaTheme.surface)
                                            .cornerRadius(8)
                                            .overlay(
                                                RoundedRectangle(cornerRadius: 8)
                                                    .stroke(isSelected ? FormaTheme.accent : FormaTheme.border, lineWidth: 1)
                                            )
                                    }
                                }
                            }
                        }

                        // Repeat Days
                        VStack(alignment: .leading, spacing: 8) {
                            Text("SCHEDULED DAYS")
                                .font(.system(size: 11, weight: .bold, design: .monospaced))
                                .foregroundColor(FormaTheme.accent)

                            HStack(spacing: 8) {
                                ForEach(dayLabels, id: \.0) { dayInt, label in
                                    let isIncluded = repeatDays.contains(dayInt)
                                    ZStack {
                                        Circle()
                                            .fill(isIncluded ? Color(hex: colorTag) : FormaTheme.surface)
                                            .frame(width: 38, height: 38)
                                            .overlay(
                                                Circle()
                                                    .stroke(isIncluded ? Color.clear : FormaTheme.border, lineWidth: 1)
                                            )

                                        Text(label)
                                            .font(.system(size: 13, weight: .semibold))
                                            .foregroundColor(isIncluded ? .white : FormaTheme.textMuted)
                                    }
                                    .onTapGesture {
                                        if repeatDays.contains(dayInt) {
                                            if repeatDays.count > 1 {
                                                repeatDays.remove(dayInt)
                                            }
                                        } else {
                                            repeatDays.insert(dayInt)
                                        }
                                        ZenHaptics.selectionChanged()
                                    }
                                }
                            }
                        }

                        // Delete button if editing
                        if let existing = habitToEdit {
                            Button(action: {
                                viewModel.deleteHabit(id: existing.id)
                                presentationMode.wrappedValue.dismiss()
                            }) {
                                HStack {
                                    Image(systemName: "trash.fill")
                                    Text("Archive Habit")
                                }
                                .font(.system(size: 14, weight: .medium))
                                .foregroundColor(.red.opacity(0.8))
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 12)
                                .background(FormaTheme.surface)
                                .cornerRadius(10)
                                .overlay(
                                    RoundedRectangle(cornerRadius: 10)
                                        .stroke(Color.red.opacity(0.3), lineWidth: 1)
                                )
                            }
                            .padding(.top, 10)
                        }
                    }
                    .padding(20)
                }
            }
            .navigationBarTitle(habitToEdit == nil ? "New Ritual" : "Edit Ritual", displayMode: .inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel") {
                        presentationMode.wrappedValue.dismiss()
                    }
                    .foregroundColor(FormaTheme.textSecondary)
                }

                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Save") {
                        saveAction()
                    }
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(name.trimmingCharacters(in: .whitespaces).isEmpty ? FormaTheme.textMuted : FormaTheme.accent)
                    .disabled(name.trimmingCharacters(in: .whitespaces).isEmpty)
                }
            }
            .onAppear {
                if let habit = habitToEdit {
                    name = habit.name
                    icon = habit.icon
                    colorTag = habit.colorTag
                    timeOfDay = habit.timeOfDay
                    energyLevel = habit.energyLevel
                    stackedCueText = habit.stackedCueText ?? ""
                    repeatDays = habit.repeatDays
                }
            }
        }
    }

    private func saveAction() {
        let trimmedName = name.trimmingCharacters(in: .whitespaces)
        guard !trimmedName.isEmpty else { return }

        let habit = Habit(
            id: habitToEdit?.id ?? UUID().uuidString,
            name: trimmedName,
            icon: icon,
            colorTag: colorTag,
            timeOfDay: timeOfDay,
            energyLevel: energyLevel,
            repeatDays: repeatDays,
            createdAt: habitToEdit?.createdAt ?? Int64(Date().timeIntervalSince1970 * 1000),
            stackedCueText: stackedCueText.isEmpty ? nil : stackedCueText,
            startDate: habitToEdit?.startDate ?? DateUtils.todayString()
        )

        onSave(habit)
        presentationMode.wrappedValue.dismiss()
    }
}
