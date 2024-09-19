import {Component, OnInit} from '@angular/core';
import {ActorDto} from 'src/app/dtos/actor.dto';
import {FilterAndSortEventsInfo} from 'src/app/dtos/filter-sort-events-info.dto';
import {MovieDto} from 'src/app/dtos/movie.dto';
import {MoviesService} from 'src/app/services/movies.service';
import {RdfTurtleDialogComponent} from '../rdf-turtle-dialog/rdf-turtle-dialog.component';
import {MatDialog} from '@angular/material/dialog';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {
  movies: MovieDto[] = [];
  actors: ActorDto[] = [];
  selectedActors: ActorDto[] = [];
  sortAndFilterInfo: FilterAndSortEventsInfo = new FilterAndSortEventsInfo();
  isPageLoading: boolean = false;
  applyingFilters: boolean = false;
  showFilters: boolean = false;

  constructor(private moviesService: MoviesService, private dialog: MatDialog) {
  }

  ngOnInit(): void {
    this.isPageLoading = true;
    this.sortAndFilterInfo.isAscendantOrder = true;
    this.sortAndFilterInfo.onlyNewMovies = false;
    this.sortAndFilterInfo.actors = [];
    this.sortAndFilterInfo.sortType = "year";
    this.moviesService.getMovies(this.sortAndFilterInfo, 12).subscribe({
      next: data => {
        this.isPageLoading = false
        this.movies = data;
        this.showFilters = true;
        this.selectedActors = [];
        this.actors = this.extractUniqueActors(data.map(movie => movie.actors).flat());
      },
      error: () => {
        this.isPageLoading = false;
      }
    });
  }

  openRdfTurtleDialog(movie: any) {
    const rdfTurtle = this.getRdfTurtleForMovie(movie); // Implement this method to fetch the RDF Turtle
    this.dialog.open(RdfTurtleDialogComponent, {
      data: {rdfTurtle},
      width: '80%',
      height: '80%'
    });
  }

  private getRdfTurtleForMovie(movie: any): string {
    return movie.rdfTurtle;
  }

  applyFilters(): void {
    this.applyingFilters = true;
    this.movies = [];
    this.sortAndFilterInfo.actors = this.selectedActors.map(act => act.name);

    this.moviesService.getMovies(this.sortAndFilterInfo, 12)
      .subscribe({
        next: movies => {
          this.movies = movies;
          this.applyingFilters = false;
        },
        error: err => {
          console.log(err);
          this.applyingFilters = false;
        }
      });
  }

  compareActors(a1: ActorDto, a2: ActorDto): boolean {
    return a1.name === a2.name;
  }

  extractUniqueActors(actors: ActorDto[]): ActorDto[] {
    const uniqueActors: ActorDto[] = [];
    actors.forEach(actor => {
      if (!uniqueActors.some(act => this.compareActors(act, actor))) {
        uniqueActors.push(actor);
      }
    });
    return uniqueActors;
  }

  onActorChange(event: Event): void {
    const checkbox = event.target as HTMLInputElement;
    if (checkbox.checked) {
      this.selectedActors.push(this.actors.filter(act => act.name === checkbox.value)[0]);
    } else {
      const index = this.selectedActors.indexOf(this.actors.filter(act => act.name === checkbox.value)[0]);
      if (index > -1) {
        this.selectedActors.splice(index, 1);
      }
    }
  }

  toggleSortOrder(): void {
    this.sortAndFilterInfo.isAscendantOrder = !this.sortAndFilterInfo.isAscendantOrder;
    this.applyFilters();
  }

}
