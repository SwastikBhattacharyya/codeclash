import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ThemeToggle } from '../ui/theme-toggle';
import { Wordmark } from '../ui/wordmark';
import { BoardPanel } from './board-panel';

@Component({
  selector: 'cc-auth-layout',
  imports: [RouterOutlet, BoardPanel, ThemeToggle, Wordmark],
  templateUrl: './auth-layout.html',
})
export class AuthLayout {}
