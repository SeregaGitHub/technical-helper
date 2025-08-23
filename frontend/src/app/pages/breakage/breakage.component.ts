import { Component } from '@angular/core';
import { BreakageService } from '../../services/breakage.service';

@Component({
  selector: 'app-breakage',
  imports: [],
  templateUrl: './breakage.component.html',
  styleUrl: './breakage.component.css'
})
export class BreakageComponent {

  constructor (private _breakageService: BreakageService) { }
}
