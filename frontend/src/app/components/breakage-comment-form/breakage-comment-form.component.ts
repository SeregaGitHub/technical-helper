import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';

@Component({
  selector: 'app-breakage-comment-form',
  imports: [],
  templateUrl: './breakage-comment-form.component.html',
  styleUrl: './breakage-comment-form.component.css'
})
export class BreakageCommentFormComponent implements OnInit {

  constructor(@Inject(MAT_DIALOG_DATA) public data: any) { }

  ngOnInit(): void {
    console.log('BreakageCommentFormComponent - ngOnInit()');
    console.log(this.data.breakageId);
    console.log(this.data.status);
  }

}
