import {Component} from '@angular/core';
import {LoadingController, ToastController, Loading} from 'ionic-angular';
import {Camera} from "@ionic-native/camera";
import {FileTransfer, FileTransferObject} from "@ionic-native/file-transfer";

@Component({
  selector: 'page-home',
  templateUrl: 'home.html'
})
export class HomePage {
  imageSrc: string;
  imageUploaded: boolean = true;
  private loading: Loading;

  constructor(private readonly loadingCtrl: LoadingController,
              private readonly toastCtrl: ToastController,
              private readonly camera: Camera,
              private readonly transfer: FileTransfer) {
    this.loading = loadingCtrl.create({
      content: "Uploading picture..."
    });
  }

  upload() {
    const fileTransfer: FileTransferObject = this.transfer.create();
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
    this.camera.cleanup();
  }

  uploadFailed() {
    this.loading.dismiss();
    const toast = this.toastCtrl.create({
      message: 'Upload failed',
      duration: 3000
    });

    toast.present();
    this.camera.cleanup();
  }

  takePicture() {
    this.camera.getPicture({
      correctOrientation: true,
      destinationType: this.camera.DestinationType.FILE_URI,
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
