import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import * as Prism from 'prismjs';
import 'prismjs/components/prism-turtle';

@Component({
  selector: 'app-rdf-turtle-dialog',
  templateUrl: './rdf-turtle-dialog.component.html',
  styleUrls: ['./rdf-turtle-dialog.component.css']
})
export class RdfTurtleDialogComponent implements OnInit {
  highlightedRdfTurtle: string = "";

  constructor(@Inject(MAT_DIALOG_DATA) public data: { rdfTurtle: string }) {}

  ngOnInit() {
    this.highlightedRdfTurtle = Prism.highlight(this.data.rdfTurtle, Prism.languages['turtle'], 'turtle');
  }
}
