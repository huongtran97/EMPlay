# EMPlay - Movie and TV Show App
A mobile application that allows users to search, view, and save information about movies and TV shows, including details like cast, genres, ratings, and release dates. 
The app supports movies and TV shows, with options to save favorite content and retrieve detailed information using various APIs.

## Features
- Search for Movies & TV Shows: Users can search movies and TV shows using an intuitive search interface.
- View Details: Display detailed information such as title, rating, cast, genres, and overview of selected content.
- Toggle Between Movies and TV Shows: A toggle button allows you to easily switch between movie and TV show data views.
- Favorite Movies/TV Shows: Save favorite movies and TV shows to a local database using SQLite.
- View Cast Information: Explore the cast names and characters for movies and TV shows.
- Firebase Authentication: Users can sign up and log in with Firebase Authentication. Profile details are updated and saved through Firebase.

## Tech Stack
- Programming Language: Java
Android SDK: Android API 34 (UpsideDownCake) and above
Libraries & Dependencies:
Glide: For image loading and caching
Retrofit: For making network requests to fetch movie and TV show data
Firebase Authentication: For user authentication
SQLite: For storing and retrieving favorite movies/TV shows
Material Design Components: For modern UI design
RecyclerView: For displaying lists of movies, TV shows, and cast members
CardView: For better layout of movie/TV show details
ConstraintLayout: For flexible and efficient UI design

- API Integration
The app integrates with TMDB API to fetch movie and TV show data. You will need an API key to use this feature.

- How to Use
Search for Movies/TV Shows:
Use the search bar to enter the title of a movie or TV show.
Toggle between movie and TV show results.

- View Details:
Select a movie or TV show from the search results to view its details.

- Save to Favorites:
Click the save button to add a movie or TV show to your favorites.
Favorites can be viewed even offline from the saved data in SQLite.

- Sign Up/Log In:
Use Firebase Authentication to create an account and log in.
Your profile will display after sign-up.

## Known Issues
Sometimes, images may fail to load due to missing or invalid URLs from the API.
SQLite database inspection may not immediately show data in App Inspector, but it is confirmed to have been added correctly.






