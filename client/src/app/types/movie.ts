import { Actor } from "./actor";

export class Movie {
    name : string = '';
    year: number = 0;
    imgSrc : string = "";
    description : string = "";
    genre : string = "";
    rdfTurtle : string = "";
    url : string = "";
    actors : Actor[] = [];

    constructor(data?: any) {
        Object.assign(this, data);
    }
}