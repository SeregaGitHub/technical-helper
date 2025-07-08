import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from "./layout/header/header.component";
import { FooterComponent } from "./layout/footer/footer.component";

@Component({
  selector: 'app-root',
  imports: [
    RouterOutlet,
    HeaderComponent,
    FooterComponent
],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {


  //constructor(private service: AuthService) {}

  

    // ngOnInit(): void {
    //   console.log('AppComponent - ' + this.service.username$)
    //   console.log(this.service.username$.subscribe())
    //   console.log('AppComponent - ' + this.service.username$)
    // }
}
