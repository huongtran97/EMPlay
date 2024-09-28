# EMPlay - Movie and TV Show App
A mobile application that allows users to search, view, and save information about movies and TV shows, including details like cast, genres, ratings, and release dates. 
The app supports movies and TV shows, with options to save favorite content and retrieve detailed information using various APIs.

## 1. Features
- Search for Movies & TV Shows: Users can search movies and TV shows using an intuitive search interface.
- View Details: Display detailed information such as title, rating, cast, genres, and overview of selected content.
- Toggle Between Movies and TV Shows: A toggle button lets you easily switch between movie and TV show data views.
- Favorite Movies/TV Shows: Save favorite movies and TV shows to a local database using SQLite.
- View Cast Information: Explore the cast names and characters for movies and TV shows.
- Firebase Authentication: Users can sign up and log in with Firebase Authentication. Profile details are updated and saved through Firebase.

## 2. Screenshot
- Home Page: This is the app's home page, where users can see the top trends of the week and upcoming  movies and TV shows.
  <br>
<img src="https://github.com/user-attachments/assets/fd564b29-c3cc-48ec-8a7b-452452bc3495" alt="Screenshot_Home_Page" width="150"/>
<br><br>

- Search Page: Where users can search for movies and TV shows, a search results page where users can toggle between movies and TV shows.
  <br>
<img src="https://github.com/user-attachments/assets/e92980bd-c330-4dc3-8dfe-a99259d83fe1" alt="Screenshot_Search_Page" width="150"/>
<br><br>

- Profile Page: Where the logged-in user can manage their account.
  <br>
<img src="https://github.com/user-attachments/assets/027bc84a-6d2b-4e3e-8e01-e0afc0f7aaac" alt="Screenshot_Profile_Page" width="150"/>
<br><br>

- Sign-up Page: Firebase-powered sign-up page for new users.
 <br>
<img src="https://github.com/user-attachments/assets/9a3df08a-ff68-4dd6-98f1-7238c9f7e1cc" alt="Screenshot_SignUp_Page" width="150"/>
<br><br>

- Login Page: Firebase-powered login page for existing users.
 <br>
<img src="https://github.com/user-attachments/assets/81f2bf75-00d1-4091-ae66-e6791f200af4" alt="Screenshot_Login_Page" width="150"/>
<br><br>

- Favorites Page: A page showing the user's saved favorite movies and TV shows stored in SQLite.
  <br>
<img src="https://github.com/user-attachments/assets/71bb827a-b758-4a57-87d9-02b6de0a45c6" alt="Screenshot_Favorite_Page_1" width="150"/>
<img src="https://github.com/user-attachments/assets/e7c282dd-a9a9-4461-912e-a38d42409df2" alt="Screenshot_Favorite_Page" width="150"/>
<br><br>

## 3. Demo



https://github.com/user-attachments/assets/1bf8c0fc-5bad-427f-93d7-b61b22aa9d99



## 4. Tech Stack
- Programming Language: Java
- Android SDK: Android API 34 (UpsideDownCake) and above
- Libraries & Dependencies:
- Glide: For image loading and caching
- Retrofit: For making network requests to fetch movie and TV show data
- Firebase Authentication: For user authentication
- SQLite: For storing and retrieving favorite movies/TV shows
- Material Design Components: For modern UI design
- RecyclerView: For displaying lists of movies, TV shows, and cast members
- CardView: For better layout of movie/TV show details
- ConstraintLayout: For flexible and efficient UI design

## 5. API Integration
- The app integrates with TMDB API to fetch movie and TV show data. You will need an API key to use this feature.
  
## 6. How to Install
- Prerequisites:
  - Before installing the app, ensure that you have the following set up:
  - Android Studio (Latest version recommended)
  - Android SDK (API 34 or above)
  - Git installed on your system
  - TMDB API Key: To fetch movie and TV show data, you must generate a TMDB API key. Sign up and generate an API key [here](https://www.themoviedb.org/).

## Steps to Install the App:
1. Clone the Repository:
  - Open a terminal or command prompt on your machine.
Navigate to the directory where you want to store the project.
Run the following command to clone the project from GitHub:
  
```git clone https://github.com/your-username/EMPlay.git```

  - Replace your username with your GitHub username or the repository owner’s name.
  
2. Open the Project in Android Studio:
- Launch Android Studio.
  - From the Welcome Screen, select Open an Existing Project.
  - Navigate to the directory where the project was cloned and select the project folder.
  - Android Studio will automatically configure the project, sync the Gradle files, and install the necessary dependencies.
  
4. Add TMDB API Key:
  - In your project directory, locate the local.properties file.
  - Add your TMDB API key as follows:
  
  ```properties```
<br><br>
  ```API_KEY=your_tmdb_api_key```

  - Replace your_tmdb_api_key with the actual API key from TMDB.
  
5. Build the Project:
Once the API key is added, click the Sync Project with Gradle Files button in Android Studio (usually located at the top right corner).
After the sync, click Build > Build APK or Run to install the app on an emulator or a physical device connected to your machine.

## Run the App:

Choose a device (emulator or physical Android device) and press the Run button in Android Studio. The app will be installed and launched on the selected device.
- Firebase Setup (Optional): If you wish to use Firebase Authentication, ensure that Firebase is set up in the project. Follow Firebase setup instructions, including adding google-services.json to the app’s app/ directory. You can find instructions on how to set up Firebase here.
  
- Known Issues: For the app to run properly, ensure the API key and Firebase configurations are correctly set up. Images may fail to load if invalid or missing URLs are returned from the API.








