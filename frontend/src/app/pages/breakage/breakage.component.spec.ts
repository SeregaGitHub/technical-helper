import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BreakageComponent } from './breakage.component';

describe('BreakageComponent', () => {
  let component: BreakageComponent;
  let fixture: ComponentFixture<BreakageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BreakageComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BreakageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
