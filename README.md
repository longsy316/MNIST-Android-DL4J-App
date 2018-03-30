# MNIST-Android-App
<h3>Android app which can identify hand written (single) digits.</h3>
<ul>
<li>The app is built in Java using Deeplearning4j library. The model is a 3 layer convolutional neural network which detects digits with ~98% accuracy.</li>
<li>The model is trained on MNIST standard dataset and saved to disk.</li>
<li>Inferencing is done by loading the saved model (from internal storage, ~3MB in size) everytime the app is started. </li>
</ul>
<br>

<img align="middle" src="https://github.com/pawanpatil94/MNIST-Android-App/blob/master/Mnist-demo.gif" height=640px width=360px>
