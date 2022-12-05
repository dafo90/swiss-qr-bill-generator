import { Injectable } from '@angular/core';
import { catchError, Observable, of } from 'rxjs';
import { logError } from '../commons/loggers/logging';
import { FieldGroup } from '../types/field-group';
import { AbstractApiService } from './abstract-api.service';

@Injectable({
  providedIn: 'root',
})
export class FieldGroupService extends AbstractApiService {
  protected override context = 'field-groups';

  getAll(): Observable<FieldGroup[]> {
    const uri = `${this.baseUri}/${this.context}`;
    return this.http
      .get<FieldGroup[]>(uri, {
        headers: this.defaultHeaders(),
      })
      .pipe(
        catchError((err) => {
          logError(err);
          return of([]);
        })
      );
  }
}
