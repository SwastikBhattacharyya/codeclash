import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import {
  PASSWORD_URL,
  type PasswordChangeResult,
  type ProfileChanges,
  type SignUpRequest,
  USERS_URL,
  type User,
  userUrl,
  VERIFY_EMAIL_URL,
} from './user';

@Service()
export class AccountApi {
  private readonly http = inject(HttpClient);

  signUp(request: SignUpRequest): Promise<User> {
    return firstValueFrom(this.http.post<User>(USERS_URL, request));
  }

  verifyEmail(username: string, token: string): Promise<void> {
    return firstValueFrom(this.http.post<void>(VERIFY_EMAIL_URL, { username, token }));
  }

  updateProfile(id: string, changes: ProfileChanges): Promise<User> {
    return firstValueFrom(this.http.patch<User>(userUrl(id), changes));
  }

  changePassword(currentPassword: string, newPassword: string): Promise<PasswordChangeResult> {
    return firstValueFrom(
      this.http.post<PasswordChangeResult>(PASSWORD_URL, { currentPassword, newPassword }),
    );
  }
}
