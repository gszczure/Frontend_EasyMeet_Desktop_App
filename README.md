> [!IMPORTANT]  
> This repository **will no longer be maintained or updated**.  
> No further improvements or changes will be made.  
>  
> It contains the **frontend of the EasyMeet App (desktop version)**.
> This desktop version of the MeetMe app was the **pre-web version** of the application.  
> The web version is still being actively updated.
> New Version Available: 🌐 [MeetMe Web App](https://meetme-web-q5ol.onrender.com/)  |  📂 [EasyMeetApp Repo](https://github.com/gszczure/EasyMeet_App)

# EasyMeetApp

EasyMeetApp is an application for organizing meetups, allowing users to create and join events, such as "BBQ in Pisary." The app was created to address the challenge of finding a convenient date for a gathering among a larger group of friends, where each person had different time preferences. Thanks to the app, finding a common date for a meeting has become much easier.

Below, you'll find details about installation, technologies, and the app's core features.

## Features

- **Event creation**: Registered users can create their own events.
- **Sharing codes**: Each event generates a unique code that can be shared with other users. By entering this code, they can join the event.
- **Participant management**: The event owner can remove participants, and participants can voluntarily leave the event.
- **Meeting date selection**:
  - Users can add any number of date ranges that suit them.
  - Each user can **only delete their own date ranges**, not those of other participants.
  - The app shows common available dates for all participants. The event owner has the option to select the final date from the available options.

## Technologies

- **Backend**: The backend code is available on GitHub: [BACKENDMEETINGAPP](https://github.com/gszczure/BACKENDMEETINGAPP). The backend is built using **Java** and **Spring**, requiring a minimum version of 14 (Oracle Java SDK 19 recommended).
- **Frontend**: The frontend was developed using **JavaFX** and **Scene Builder** with some **CSS** for styling.
- **Website**: I am also working on the web version of the application. [Website](https://meetme-web-q5ol.onrender.com/). [Repository](https://github.com/gszczure/MeetMe_Web_App) (Currently, on the first visit to the page, registration or login may take longer to load)
- **Authentication**: User authentication is managed using **JWT (JSON Web Token)**, ensuring secure session management.
- **Database**: The app uses a **PostgreSQL** database, and user passwords are **hashed** before being stored in the database for an additional layer of security.
- **Hosting**:
  - The backend is hosted on **Render**.
  - The database is hosted on **Railway**.

## See the demonstration of the first version of the application

[Watch the demonstration on YouTube](https://youtu.be/fVYEp7d8_mM)

## System Requirements

To run the application, the following is required:
- **Java** version 14 or higher (Oracle Java SDK 19 recommended).

Check your Java installation using the command:
`java -version`

## Installation

1. Download and install the application.
2. By default, the application installs in the directory `C:\\ProgramFiles\\MeetMeApp`.
3. Locate the `.exe` file in the installation folder. **Do not move it directly to the desktop!** Instead, create a shortcut to this file on your desktop to ensure the application runs correctly.

## Performance Note

- The application may run **slowly during the first launch** because the server needs to start up. The wait time can be up to 4 minutes when logging in or registering.
- Once the server is running, the application should operate faster.

## Testing and Feedback

The application is in development. Currently, I am working on the second version on a separate branch and also on a version of the same application that functions as a website. All feedback, suggestions for improvements, changes, or feature removals are welcome.

A test event is available with the code: `af86a`.
