import { Component, inject, RESPONSE_INIT } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Button } from '../../ui/button';
import { Wordmark } from '../../ui/wordmark';

const NOT_FOUND_STATUS = 404;

@Component({
  selector: 'cc-not-found',
  imports: [RouterLink, Button, Wordmark],
  templateUrl: './not-found.html',
})
export class NotFound {
  constructor() {
    const responseInit = inject(RESPONSE_INIT);
    if (responseInit) {
      responseInit.status = NOT_FOUND_STATUS;
    }
  }
}
