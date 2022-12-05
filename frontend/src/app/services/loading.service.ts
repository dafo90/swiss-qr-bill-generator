import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { Status } from '../types/status';

@Injectable({
  providedIn: 'root',
})
export class LoadingService {
  private statusSubject = new BehaviorSubject<Status>(Status.READY);
  public status$ = this.statusSubject.asObservable();

  constructor() {}

  loading(): void {
    this.statusSubject.next(Status.LOADING);
  }

  ready(): void {
    this.statusSubject.next(Status.READY);
  }
}
