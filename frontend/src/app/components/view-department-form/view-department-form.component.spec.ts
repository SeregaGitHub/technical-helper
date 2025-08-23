import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ViewDepartmentFormComponent } from './view-department-form.component';

describe('ViewDepartmentFormComponent', () => {
  let component: ViewDepartmentFormComponent;
  let fixture: ComponentFixture<ViewDepartmentFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ViewDepartmentFormComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ViewDepartmentFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
