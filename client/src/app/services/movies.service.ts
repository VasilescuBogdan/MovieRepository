import {HttpClient, HttpHeaders, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {FilterAndSortEventsInfo} from '../dtos/filter-sort-events-info.dto';
import {MovieDto} from '../dtos/movie.dto';

const API_URL = 'http://localhost:8080/api/v1/movies';

@Injectable({
  providedIn: 'root'
})
export class MoviesService {

  constructor(private http: HttpClient) {
  }

  init(url: string, maximumMovies: number): Observable<Boolean> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
    });

    // Build the query parameters
    let params = new HttpParams();
    params = params.set('maximumMovies', maximumMovies.toString()); // Ensure maximumMovies is a string
    params = params.set('url', url);

    // Use the params and headers as part of the options argument
    return this.http.post<Boolean>(API_URL + "/init", null, {headers, params});
  }


  getMovies(filterAndSortEventsInfo: FilterAndSortEventsInfo, maximumMovies: number): Observable<MovieDto[]> {
    let params = new HttpParams();
    params = params.set('maximumMovies', maximumMovies.toString()); // Ensure maximumMovies is a string

    return this.http.post<MovieDto[]>(API_URL, filterAndSortEventsInfo, {params});
  }

}
