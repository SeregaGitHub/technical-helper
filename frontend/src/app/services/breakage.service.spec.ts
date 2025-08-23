import { TestBed } from '@angular/core/testing';

import { BreakageService } from './breakage.service';

describe('BreakageService', () => {
  let service: BreakageService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(BreakageService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
