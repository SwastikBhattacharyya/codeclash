import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';

export type WordmarkSize = 'sm' | 'xl';

@Component({
  selector: 'cc-wordmark',
  imports: [RouterLink],
  templateUrl: './wordmark.html',
})
export class Wordmark {
  readonly size = input<WordmarkSize>('sm');
}
