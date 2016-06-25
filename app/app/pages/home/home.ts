import {Component} from "@angular/core";
import {NavController, Loading, Toast} from 'ionic-angular';
import {Camera, Transfer} from 'ionic-native';

@Component({
  templateUrl: 'build/pages/home/home.html'
})
export class HomePage {
  private imageSrc: string;
  private imageUploaded: boolean = true;
  private loading: Loading;

  constructor(private navController: NavController) {
    this.loading = Loading.create({
      content: "Uploading picture..."
    });
  }

  upload() {
    const fileTransfer = new Transfer();

    this.navController.present(this.loading);
    fileTransfer.upload(this.imageSrc, "https://demo.rasc.ch/vision/pictureupload")
      .then(this.uploadSuccessful.bind(this))
      .catch(this.uploadFailed.bind(this));
  }

  uploadSuccessful() {
    this.imageUploaded = true;
    this.loading.dismiss();

    const toast = Toast.create({
      message: 'Successfully uploaded',
      duration: 3000
    });

    this.navController.present(toast);
    Camera.cleanup();
  }

  uploadFailed() {
    this.loading.dismiss();
    const toast = Toast.create({
      message: 'Upload failed',
      duration: 3000
    });

    this.navController.present(toast);
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
