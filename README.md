# TwoCars - Collect & Dodge

An F1-themed "Collect & Dodge" game built with Jetpack Compose.

## Remote Config: F1 Team Names

All F1 team display names are driven by **Firebase Remote Config**, allowing you to update them without any code changes.

### How It Works

- Each team has a **stable internal ID** (e.g., `red_bull`, `ferrari`) defined in `F1Team.kt`.
- Display names are fetched from the `team_display_names` Remote Config key.
- If a team ID has no Remote Config override, the default display name from `F1Team` is used.

### Remote Config Key: `team_display_names`

Set this key to a JSON object mapping team IDs to display names:

```json
{
  "mclaren": "McLaren",
  "red_bull": "Oracle Red Bull Racing",
  "mercedes": "Mercedes-AMG Petronas",
  "ferrari": "Scuderia Ferrari",
  "aston_martin": "Aston Martin Aramco",
  "alpine": "BWT Alpine",
  "williams": "Williams Racing",
  "haas": "MoneyGram Haas F1 Team",
  "racing_bulls": "Racing Bulls",
  "audi": "Audi F1 Team",
  "cadillac": "Cadillac F1 Team"
}
```

### How to Update a Team Name

1. Go to **Firebase Console → Remote Config**
2. Edit the `team_display_names` key
3. Change the value for the team ID (e.g., `"red_bull": "New Red Bull Name"`)
4. **Publish** the changes
5. The app will pick up the new name on the next launch — no code changes needed

### How to Add a New Team

1. Add a new entry to `F1Team` enum in `F1Team.kt` with a unique `id` and `defaultDisplayName`
2. Add a color theme in `Theme.kt` and a `GameBackground` entry
3. Update `getGameTheme()` in `Theme.kt` to map the new team
4. Add the team's background image URL to the `background_images` Remote Config key
5. Add a display name entry to the `team_display_names` Remote Config key

### How to Remove a Team

1. Remove the entry from the `F1Team` enum in `F1Team.kt`
2. Remove the color theme and `GameBackground` entry from `Theme.kt`
3. Remove the mapping from `getGameTheme()` in `Theme.kt`
4. Remove the team's entries from both `background_images` and `team_display_names` in Remote Config