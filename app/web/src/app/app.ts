import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ToastOutlet } from './ui/toast-outlet';

@Component({
  imports: [RouterOutlet, ToastOutlet],
  selector: 'cc-root',
  templateUrl: './app.html',
})
export class App {}
