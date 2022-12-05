import { Component, Input } from '@angular/core';
import { Field } from 'src/app/types/field';

@Component({
  selector: 'app-field',
  templateUrl: './field.component.html',
  styleUrls: ['./field.component.css'],
})
export class FieldComponent {
  @Input() field: Field;
}
