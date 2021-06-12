# Face Detection using Google Vision
This module is responsible for face detection using Google's vision library
It also gives an overview of face `Liveness detection`

## Why detecting face liveness?
Most facial detection algorithms you find on the internet suffer from photo attacks. These methods work really well at detecting and recognizing faces on images. However they can’t distinguish between real life faces and faces on a photo. This inability to recognize faces is due to the fact that these algorithms work on 2D frames.

Now let’s imagine we want to implement a facial recognition door opener. The system would work well to distinguish between known faces and unknown faces so that only authorized persons have access. Nonetheless, it would be easy for an ill-intentioned person to enter by only showing an authorized person’s photo.

## Solution
The objective of this repository is to implement an eye-blink detection-based face liveness detection algorithm to thwart photo attacks. The algorithm works in real time through a mobile device and detect face only if they blinked.

Lets look into the video:
