# Instamurom

## Setup

Go to our discord channel, look for the `secret` thread, and get the `google-services.json` file, and put it in the `project_folder/app` folder.

## Guide

### Setup before hack (IMPORTANT)

#### Install an Emulator that has Play Store

Open Play Store app in your emulator:

1. Login with Google (you can pick the `fitus.edu.vn` account for data safety)
2. Search and update (or install) the "Google Play Services"

#### Add your fingerprint

Why do we need to add a fingerprint? Because we need to make sure that the app is running on the right device, Google Play Store uses the fingerprint to verify the app.

To add your fingerprint, follow these steps:

1. Open the terminal
2. Go to the project folder
3. Run `./gradlew signingReport` or `keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android`
4. Copy the SHA1 and SHA256
5. Give these keys to @devtronghieu, he will add them to the firebase console

### How to contribute your code

1. Create a new branch for your task (for example, @trungle picked a ui task that builds a profile screen for our app, he would create a branch named `trungle/profile-screen`
2. Execute your task (during the development process, you can push your code to your remote branch e.g. `origin trungle/profile-screen`, and create a draft pull request to ask for help)
3. When you are ready, [commit](https://github.com/conventional-changelog/commitlint) and make it a pull request, and assign yourself ask the one who executed the task as well as require your teammates to review your code
4. Resolve the comments from other teammates
5. Wait for at least 1 approval
6. Merge your code
