import { Component } from '@angular/core';
import {Location} from '@angular/common';

@Component({
  selector: 'app-not-found',
  imports: [],
  templateUrl: './not-found.component.html',
  styleUrl: './not-found.component.css'
})
export class NotFoundComponent {

    constructor(private _location: Location) {}

    backClicked() {
      this._location.back();
    }
}
