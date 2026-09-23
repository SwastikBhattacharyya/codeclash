package com.codeclash.contestservice.user.port.outbound;

import com.codeclash.contestservice.user.model.Id;
import java.util.List;
import java.util.UUID;

public interface ContestLeaderboards {

  List<UUID> findRegisteredContestIds(Id id);

  void lock(List<UUID> contestIds);

  void rebuild(List<UUID> contestIds);
}
