import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MaterialModule } from '../commons/material.module';
import { FieldComponent } from './field/field.component';
import { FormComponent } from './form.component';
import { GroupComponent } from './group/group.component';
import { GroupsComponent } from './groups/groups.component';

@NgModule({
  declarations: [
    FormComponent,
    GroupsComponent,
    GroupComponent,
    FieldComponent,
  ],
  imports: [CommonModule, MaterialModule],
})
export class FormModule {}
