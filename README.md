# TwoCars: Collect & Dodge 

TwoCars is an adrenaline-fueled, F1-themed "Collect & Dodge" game built entirely with modern Android technologies. In this fast-paced game, you are tasked with controlling two cars simultaneously. Each car has two lanes, and you must switch lanes to collect targets, dodge obstacles, and survive as long as possible!

The target is to build a high score, master the combo system, and unlock your favorite F1 teams through sheer skill.

## Features 
* **Dual Car Control**: Manage two vehicles at the exact same time! Car 1 navigates the left two lanes, while Car 2 navigates the right two lanes.
* **Collect & Dodge**: Collect the designated targets to increase your score and carefully dodge obstacles to avoid a game over.
* **Power-Ups**: Gain a competitive edge with dynamic power-ups like:
  * **Magnet**: Automatically pulls collectibles towards your cars.
  * **Shield**: Protects you from a single collision with an obstacle.
  * **Slow-Mo**: Reduces the speed of the game, giving you extra reaction time.
  * **Double Points**: Multiplies your scoring potential temporarily.
  * **Ghost**: Allows you to pass through obstacles unharmed.
* **Progressive Difficulty**: The game naturally speeds up and gets harder as your score increases!
* **Combo System & Achievements**: Chain collections together to build combo multipliers. Accomplish in-game tasks (e.g., Near Misses, Survival Time) to unlock special achievements.
* **Unlockable F1 Teams**: Accumulate a global cumulative score across runs to unlock iconic F1 constructors (e.g., Red Bull, Ferrari, McLaren, Mercedes).
* **Multiple Game Modes**: Play the classic Endless survival mode or take on the 90-second Timed Challenge.
* **Remote Config Support**: 
  * Dynamically update F1 team display names without requiring an app update.
  * Change team backgrounds and visual configurations remotely.

## Screenshots 
![Image](https://github.com/user-attachments/assets/67eafa69-04a7-420f-bea0-71c440a9364f)

## Getting Started 
Clone the repository to get started with the game:
```bash
git clone https://github.com/amEya911/TwoCars.git
```

1. Open the project in **Android Studio**.
2. Sync the Gradle files to download the necessary dependencies.
3. Build and run the project on an emulator or a physical Android device.

## How to Play 
1. **Tap Left Side**: Switches the lane of the left car.
2. **Tap Right Side**: Switches the lane of the right car.
3. **Objective**: Collect all the circle targets. If you miss a target, the game ends.
4. **Avoid**: Do not hit the square obstacles. Hitting an obstacle ends the game immediately.
5. **Score Big**: Pick up special power-ups, chain targets for combos, and pull off "Near Misses" for bonus points!

## Technical Overview 
The TwoCars app leverages the power of modern Android development frameworks and robust architectural patterns to ensure a smooth 60 FPS gameplay experience without relying on a third-party game engine.

* **Jetpack Compose**: Used exclusively for building the entire UI, including the real-time custom game canvas rendering, particles, and animations.
* **Kotlin & Coroutines**: The primary language for app development. Coroutines and Flows are used to handle asynchronous data streams and the high-performance game loop.
* **MVI / Event-Driven Architecture**: Unidirectional Data Flow ensures UI state is predictably driven by interactions (`GameEvent`). The entire core game loop and state management live inside the `GameViewModel`.
* **Custom Game Engine**: Features a bespoke loop that uses delta-time calculations to interpolate movement, spawn entities, and compute hit-boxes seamlessly.
* **Preferences DataStore**: Replaces SharedPreferences for safe, asynchronous local persistence of high scores, cumulative scores, unlocked teams, and achievements.
* **Dagger Hilt**: Used for robust dependency injection across ViewModels.
* **Firebase Remote Config & Crashlytics**: Ensures real-time updates for theming/team names and active crash monitoring in production.

## Contributions 
Feel free to contribute by opening issues or submitting pull requests. All contributions, bug reports, and feature requests are welcome!
