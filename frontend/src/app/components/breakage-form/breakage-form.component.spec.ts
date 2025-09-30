import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BreakageFormComponent } from './breakage-form.component';

describe('BreakageFormComponent', () => {
  let component: BreakageFormComponent;
  let fixture: ComponentFixture<BreakageFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BreakageFormComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BreakageFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
