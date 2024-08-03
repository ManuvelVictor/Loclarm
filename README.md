# Location-Based Alarm

A location-based alarm Android application built using Kotlin and MVVM architecture. This app allows users to set alarms based on specific locations and notifies them when they reach the specified area.

## Screenshots

<p align="center">
  <img src="https://github.com/ManuvelVictor/Loclarm/blob/aa13a72a4f8da7c08b69f8e1f06eec9410792553/loclarm_home.PNG" width="300" height="600"/>
  <img src="https://github.com/ManuvelVictor/Loclarm/blob/03a88f83ccf5b1aa6c3383a349e330aa5fa6755b/loclarm_search.PNG" width="300" height="600"/>
</p>
<p align="center">
  <img src="https://github.com/ManuvelVictor/Loclarm/blob/03a88f83ccf5b1aa6c3383a349e330aa5fa6755b/loclarm_set_location.PNG" width="300" height="600"/>
  <img src="https://github.com/ManuvelVictor/Loclarm/blob/03a88f83ccf5b1aa6c3383a349e330aa5fa6755b/loclarm_notification.PNG" width="300" height="600"/>
</p>
<p align="center">
  <img src="https://github.com/ManuvelVictor/Loclarm/blob/03a88f83ccf5b1aa6c3383a349e330aa5fa6755b/loclarm_navigation_menu.PNG" width="300" height="600"/>
  <img src="https://github.com/ManuvelVictor/Loclarm/blob/03a88f83ccf5b1aa6c3383a349e330aa5fa6755b/loclarm_without_alarms.PNG" width="300" height="600"/>
</p>
<p align="center">
  <img src="https://github.com/ManuvelVictor/Loclarm/blob/03a88f83ccf5b1aa6c3383a349e330aa5fa6755b/loclarm_with_alarms.PNG" width="300" height="600"/>
  <img src="https://github.com/ManuvelVictor/Loclarm/blob/aa13a72a4f8da7c08b69f8e1f06eec9410792553/loclarm_settings.PNG" width="300" height="600"/>
</p>

## Features

- **Google Maps Integration**: Display maps and search locations.
- **Location-based Alarms**: Set alarms for specific locations with customizable radius.
- **Background Location Tracking**: Track user location even when the app is closed.
- **Custom Notifications**: Local notifications with alarm details and dismiss functionality.
- **Navigation Drawer**: Easy navigation with Home, Alarms, and Settings sections.
- **User Settings**: Customize app language, distance units, alarm ringtone, volume level, and vibration.

## Permissions

The app requires the following permissions:

- Internet
- Fine and Coarse Location
- Vibrate
- Post Notification
- Foreground Service
- Access Background Location

## Getting Started

### Prerequisites

1. Create a [Google Cloud](https://cloud.google.com/) account.
2. Enable the Google Maps API and generate an API key.
3. Link a billing account to proceed with the API usage.
4. Enable necessary APIs like the Google Places API for location search.

### Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/ManuvelVictor/Loclarm.git
   ```

2. Add your Google Maps API key to the project.

3. Open the project in Android Studio.

4. Build and run the app on your device.

## Project Structure

### MainActivity

- Declares the app to be full screen (edge-to-edge).
- Requests location and notification permissions at the start of the application.

### HomeFragment

- Implements a `MapView` that matches the full screen.
- Adds a long-click functionality to add an alarm using a Material Alert Dialog.
- Implements a robust location search feature.
- Includes a current location button to fetch and center the map on the user's location.

### Alarm Management

- Uses Room database to store alarm details (ID, name, location, radius, isActive).
- LocationService class tracks user location in the background and triggers the alarm when the specified location is reached.
- Local notifications inform the user about location tracking and alarm status.
- MediaPlayer plays the user-selected ringtone when the alarm is triggered.

### Navigation Drawer

- **Home**: Main screen with MapView and alarm creation.
- **Alarms**: List of user-created alarms with options to edit, delete, activate, or deactivate.
- **Settings**: Allows users to set app language, units for radius distance, ringtone, volume level, and vibration option.

### User Settings

- Settings are saved in a local database and used throughout the app.

## What I Learned

- Setting up a Google Cloud account and enabling Google Maps API.
- Handling permissions for various app functionalities.
- Implementing a full-screen main activity with location and notification permissions.
- Creating and managing a Room database for storing alarms.
- Background location tracking and handling foreground services.
- Using Material Alert Dialogs for user interactions.
- Implementing MVVM architecture with ViewModels for each screen.
- Coding entirely in Kotlin for Android development.

## Contributing

Contributions are welcome! Please open an issue or submit a pull request for any improvements or suggestions.

## License

This project is licensed under the Apache License.

---
