# MNIST-Android-App
Android app which can identify hand written (single) digits. <br>
The app is built in Java using Deeplearning4j library. The model is a 3 layer convolutional neural network which detects digits with ~98% accuracy. <br>
The model is trained on MNIST standard dataset and saved to disk.<br> 
Inferencing is done by loading the saved model (from internal storage, ~3MB in size) everytime the app is started.

<img src="https://github.com/pawanpatil94/MNIST-Android-App/blob/master/MNIST-app-demo.gif" height=640px width=360px>
