import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { FilterAndSortEventsInfo } from '../types/filter-sort-events-info';
import { Movie } from '../types/movie';

const API_URL = 'http://localhost:8080/api/v1/movies';

@Injectable({
  providedIn: 'root'
})
export class MoviesService {

  constructor(private http: HttpClient) { }

  init(url: string, maximumMovies: number): Observable<Boolean> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
    });

    // Build the query parameters
    let params = new HttpParams();
    params = params.set('maximumMovies', maximumMovies.toString()); // Ensure maximumMovies is a string
    params = params.set('url', url);

    // Use the params and headers as part of the options argument
    return this.http.post<Boolean>(API_URL + "/init", null, { headers, params });
  }


  getMovies(filterAndSortEventsInfo: FilterAndSortEventsInfo): Observable<Movie[]> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
    });

    return this.http.post<Movie[]>(API_URL, filterAndSortEventsInfo, {headers: headers});
  }

}
