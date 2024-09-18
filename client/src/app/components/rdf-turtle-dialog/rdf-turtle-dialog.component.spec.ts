import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RdfTurtleDialogComponent } from './rdf-turtle-dialog.component';

describe('RdfTurtleDialogComponent', () => {
  let component: RdfTurtleDialogComponent;
  let fixture: ComponentFixture<RdfTurtleDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RdfTurtleDialogComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RdfTurtleDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
