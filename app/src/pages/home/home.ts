import { Component } from '@angular/core';
import { LoadingController, ToastController, Loading } from 'ionic-angular';
import { Camera, Transfer } from 'ionic-native';

@Component({
  selector: 'page-home',
  templateUrl: 'home.html'
})
export class HomePage {
  imageSrc: string;
  imageUploaded: boolean = true;
  private loading: Loading;

  constructor(public loadingCtrl: LoadingController, private toastCtrl: ToastController) {
    this.loading = loadingCtrl.create({
      content: "Uploading picture..."
    });
  }

  upload() {
    const fileTransfer = new Transfer();

    this.loading.present();
    fileTransfer.upload(this.imageSrc, "https://demo.rasc.ch/vision/pictureupload")
      .then(this.uploadSuccessful.bind(this))
      .catch(this.uploadFailed.bind(this));
  }

  uploadSuccessful() {
    this.imageUploaded = true;
    this.loading.dismiss();

    const toast = this.toastCtrl.create({
      message: 'Successfully uploaded',
      duration: 3000
    });

    toast.present();
    Camera.cleanup();
  }

  uploadFailed() {
    this.loading.dismiss();
    const toast = this.toastCtrl.create({
      message: 'Upload failed',
      duration: 3000
    });

    toast.present();
    Camera.cleanup();
  }

  takePicture() {
    Camera.getPicture({
      correctOrientation: true,
      destinationType: Camera.DestinationType.FILE_URI,
      targetWidth: 1000,
      targetHeight: 1000
    }).then(imageFileUrl => {
      this.imageSrc = imageFileUrl;
      this.imageUploaded = false;
    }).catch(err => {
      console.log(err);
    });
  }
}
