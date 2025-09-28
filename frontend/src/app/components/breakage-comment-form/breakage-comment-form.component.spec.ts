import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BreakageCommentFormComponent } from './breakage-comment-form.component';

describe('BreakageCommentFormComponent', () => {
  let component: BreakageCommentFormComponent;
  let fixture: ComponentFixture<BreakageCommentFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BreakageCommentFormComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BreakageCommentFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
