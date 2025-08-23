import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ViewUserFormComponent } from './view-user-form.component';

describe('ViewUserFormComponent', () => {
  let component: ViewUserFormComponent;
  let fixture: ComponentFixture<ViewUserFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ViewUserFormComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ViewUserFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
