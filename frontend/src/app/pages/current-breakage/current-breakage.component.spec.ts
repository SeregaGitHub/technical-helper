import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CurrentBreakageComponent } from './current-breakage.component';

describe('CurrentBreakageComponent', () => {
  let component: CurrentBreakageComponent;
  let fixture: ComponentFixture<CurrentBreakageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CurrentBreakageComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CurrentBreakageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
