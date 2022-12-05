import { Component, OnInit } from '@angular/core';
import { logInfo } from 'src/app/commons/loggers/logging';
import { FieldGroupService } from 'src/app/services/field-group.service';
import { LoadingService } from 'src/app/services/loading.service';
import { FieldGroup } from 'src/app/types/field-group';

@Component({
  selector: 'app-groups',
  templateUrl: './groups.component.html',
  styleUrls: ['./groups.component.css'],
})
export class GroupsComponent implements OnInit {
  protected fieldGroups: FieldGroup[] = [];

  constructor(
    private fieldGroupService: FieldGroupService,
    private loadingService: LoadingService
  ) {}

  ngOnInit(): void {
    this.loadingService.loading();
    this.fieldGroupService.getAll().subscribe((fieldGroups) => {
      this.fieldGroups = fieldGroups;
      logInfo('fieldGroups found!', fieldGroups);
      this.loadingService.ready();
    });
  }
}
