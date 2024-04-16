package com.github.learntocode2013;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import jdk.incubator.concurrent.StructuredTaskScope;

// Purpose: Demonstrate short-circuiting pattern - fail pending tasks if any one task fails
public class DemoShutdownOnFailure {
  private final UserInfoRepository userInfoRepository = new UserInfoRepository();
  private final FollowersRepository followersRepository = new FollowersRepository();

  private CompleteUserProfile composeUserProfile(String userId) {
    try(final var scope = new StructuredTaskScope.ShutdownOnFailure()){

      Future<List<Follower>> followersFutSubTask = scope.fork(getFollowers(userId));

      Future<UserFollowersCount> followerCountFutSubTask = scope.fork(
          getUserFollowersCountCallable(userId));

      Future<UserInfo> userInfoFutSubTask = scope.fork(getUserInfo(userId));

      // Wait until all subtasks complete or at least one fails.
      // Fail right here if any task failed. We do not want to proceed processing
      // the subtask futures
      scope.join().throwIfFailed();

      return handleOutcomeFromSubTaskResults(
          userInfoFutSubTask,
          followerCountFutSubTask,
          followersFutSubTask);
    } catch (InterruptedException | ExecutionException ex) {
      throw new RuntimeException(ex);
    }
  }

  private static CompleteUserProfile handleOutcomeFromSubTaskResults(Future<UserInfo> userInfoFutSubTask,
      Future<UserFollowersCount> followerCountFutSubTask,
      Future<List<Follower>> followersFutSubTask) throws InterruptedException, ExecutionException {
    UserInfo userInfo = userInfoFutSubTask.get();
    UserFollowersCount userFollowersCountMeta = followerCountFutSubTask.get();

    List<Follower> followers = followersFutSubTask.get();

    return new CompleteUserProfile(
        userInfo.getId(),
        userInfo.getName(),
        Period.between(userInfo.getBirthDate(), LocalDate.now()).getYears(),
        followers,
        userFollowersCountMeta.getFollowersCount()
    );
  }

  private Callable<UserInfo> getUserInfo(String userId) {
    return () -> userInfoRepository.findUserInfoById(userId);
  }

  private Callable<UserFollowersCount> getUserFollowersCountCallable(String userId) {
    return () -> followersRepository.findFollowersCountByUserId(userId);
  }

  private Callable<List<Follower>> getFollowers(String userId) {
    return () -> followersRepository.findFollowersByUserId(userId);
  }

  public static void main(String[] args){
    var testSubject = new DemoShutdownOnFailure();
    System.out.printf("%s %n",testSubject.composeUserProfile("learntocode2013"));
  }
}
