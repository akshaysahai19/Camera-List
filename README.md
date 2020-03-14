This is a camera app where a user can upload images from gallery/custom camera or both.

The custom camera allows user to capture multiple images and enable them to zoom and crop those images inside the camera frame only.

Once you open the app, click on the upload button, add the images from gallery or camera.
Edit or remove the images
Upload all the images to firestore

Now from the home screen, press the download button to fetch and show all the images in a recyclerview.


The app currently uses MVVM architecture and makes use of the MutableLiveData along with third party libraries like Glide(image loading) and firebase(backend) for other functionalities.
