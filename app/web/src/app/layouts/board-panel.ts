import { Component } from '@angular/core';
import { Wordmark } from '../ui/wordmark';

type BoardRow = {
  rank: number;
  handle: string;
  solved: number;
  penalty: number;
};

@Component({
  selector: 'cc-board-panel',
  imports: [Wordmark],
  templateUrl: './board-panel.html',
})
export class BoardPanel {
  protected readonly rows: readonly BoardRow[] = [
    { rank: 1, handle: 'kestrel_dp', solved: 7, penalty: 412 },
    { rank: 2, handle: 'nullptr_ana', solved: 7, penalty: 498 },
    { rank: 3, handle: 'segtree_sam', solved: 6, penalty: 377 },
    { rank: 4, handle: 'off_by_juan', solved: 6, penalty: 455 },
    { rank: 5, handle: 'big_o_bea', solved: 5, penalty: 290 },
  ];
}
