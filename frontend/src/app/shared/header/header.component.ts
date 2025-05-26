import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonToggleModule} from '@angular/material/button-toggle';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../auth/service/auth.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-header',
  imports: [
    CommonModule,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatButtonToggleModule,
    RouterModule
  ],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent implements OnInit {
    title = 'Technical Helper';

    // private name?: Subscription;
    // private role?: Subscription;
    // username: any;
    // userRole: any;

    nameFromStorage: any;
    roleFromStorage: any;

    constructor(private service: AuthService, private router: Router) {}

    ngOnInit(): void {
      // this.name?.unsubscribe();
      // this.role?.unsubscribe();
      // this.name = this.service.username$.subscribe((name) => this.username = name);
      // this.role = this.service.userRole$.subscribe((role) => this.userRole = role);

      // console.log('nameFromStorage - ' + this.nameFromStorage);
      // console.log('roleFromStorage - ' + this.roleFromStorage);
      this.nameFromStorage = localStorage.getItem('thUn');
      this.roleFromStorage = localStorage.getItem('thUr');
      // console.log('nameFromStorage - ' + this.nameFromStorage);
      // console.log('roleFromStorage - ' + this.roleFromStorage);
    }

    // ngOnDestroy(): void {
  	// 	this.name?.unsubscribe();
    //   this.role?.unsubscribe();
		// }

    logout() {
      //this.name?.unsubscribe();
      //this.role?.unsubscribe();
      this.service.logout();
      // this.username = null;
      // this.userRole = null;
      this.router.navigate(["/auth"]);
    }
}
