package com.timsanalytics.crc.auth.authCommon.services;

import com.timsanalytics.crc.auth.authCommon.beans.Role;
import com.timsanalytics.crc.auth.authCommon.beans.User;
import com.timsanalytics.crc.auth.authCommon.dao.UserDao;
import com.timsanalytics.crc.utils.PrintObjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.List;

@Service
public class UserService {
    private final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private final com.timsanalytics.crc.auth.authCommon.dao.UserDao UserDao;
    private final PlatformTransactionManager mySqlAuthTransactionManager;
    private PrintObjectService printObjectService;
    private final UserRoleService userRoleService;

    @Autowired
    public UserService(UserDao UserDao,
                       PlatformTransactionManager mySqlAuthTransactionManager,
                       PrintObjectService printObjectService,
                       UserRoleService userRoleService) {
        this.UserDao = UserDao;
        this.mySqlAuthTransactionManager = mySqlAuthTransactionManager;
        this.userRoleService = userRoleService;
    }

    public User createUser(User User, User loggedInUser) {
        this.logger.debug("UserService -> createUser: username=" + User.getUsername());
        TransactionDefinition txDef = new DefaultTransactionDefinition();
        TransactionStatus txStatus = mySqlAuthTransactionManager.getTransaction(txDef);
        User user;
        try {
            // Create the new User.
            user = this.UserDao.createUser(User, loggedInUser);
            this.printObjectService.PrintObject("UserService -> createUser: User", user);

            // Add the Roles to the new User.
            for (int i = 0; i < User.getRoles().size(); i++) {
                this.userRoleService.createUserRole(
                        user.getUserGuid(),
                        User.getRoles().get(i).getRoleGuid(),
                        User.getRoles().get(i).getStatus(),
                        loggedInUser
                );
            }

            // If everything was successful, commit to the database.
            mySqlAuthTransactionManager.commit(txStatus);
            this.logger.debug("UserService -> createUser -> response: " + user);
        } catch (Exception e) {
            mySqlAuthTransactionManager.rollback(txStatus);
            logger.error("Error during creation: " + User.getUsername(), e);
            throw e;
        }
        return user;
    }

    public User getUser(String userGuid) {
        this.logger.debug("UserService -> getUser: userGuid=" + userGuid);
        return this.UserDao.getUser(userGuid);
    }

    public List<User> getUserList() {
        this.logger.debug("UserService -> getUserList");
        List<User> list = this.UserDao.getUserList();
        return list;
    }

    public User updateUser(User User, User loggedInUser) {
        this.logger.debug("UserService -> updateUser: username=" + User.getUsername());
        TransactionDefinition txDef = new DefaultTransactionDefinition();
        TransactionStatus txStatus = mySqlAuthTransactionManager.getTransaction(txDef);
        User user;
        try {
            // Update the User.
            user = this.UserDao.updateUser(User, loggedInUser);
            this.printObjectService.PrintObject("UserService -> updateUser: User", user);

            // Update the User's Roles
            for (int i = 0; i < User.getRoles().size(); i++) {
                this.userRoleService.updateUserRole(
                        user.getUserGuid(),
                        User.getRoles().get(i).getRoleGuid(),
                        User.getRoles().get(i).getStatus(),
                        loggedInUser
                );
            }

            // If everything was successful, commit to the database.
            mySqlAuthTransactionManager.commit(txStatus);
            this.logger.debug("UserService -> updateUser -> response: " + user);
        } catch (Exception e) {
            mySqlAuthTransactionManager.rollback(txStatus);
            logger.error("Error during update: " + User.getUsername(), e);
            throw e;
        }
        return user;
    }

    // DELETE
    public User deleteUser(String userGuid) {
        this.logger.debug("UserService -> deleteUser: userGuid=" + userGuid);
        User deletedUser = this.UserDao.deleteUser(userGuid);
        this.logger.debug("UserService -> deleteUser: deletedUser=" + deletedUser.getUserGuid());
        return deletedUser;
    }

    // RELATED CRUD SERVICES

    public User disableUser(String userGuid) {
        this.logger.debug("UserService -> disableUser: userGuid=" + userGuid);
        return this.UserDao.disableUser(userGuid);
    }

    public User enableUser(String userGuid) {
        this.logger.debug("UserService -> enableUser: userGuid=" + userGuid);
        return this.UserDao.enableUser(userGuid);
    }

    public User updateMyProfile(User User, User loggedInUser) {
        this.logger.debug("UserService -> updateMyProfile: userGuid=" + loggedInUser.getUserGuid());
        TransactionDefinition txDef = new DefaultTransactionDefinition();
        TransactionStatus txStatus = mySqlAuthTransactionManager.getTransaction(txDef);
        User item;
        try {
            // Update the User.
            item = this.UserDao.updateMyProfile(User, loggedInUser);
            this.printObjectService.PrintObject("UserService -> updateMyProfile: User", item);

            // If everything was successful, commit to the database.
            mySqlAuthTransactionManager.commit(txStatus);
            this.logger.debug("UserService -> updateMyProfile -> response: " + item);
        } catch (Exception e) {
            mySqlAuthTransactionManager.rollback(txStatus);
            logger.error("Error during update: " + User.getUsername(), e);
            throw e;
        }
        return item; // Note: this User does not include the updates
    }

    // NON-CRUD SERVICES

    public User changePassword(User User, User loggedInUser) {
        this.logger.debug("UserService -> changePassword");
        TransactionDefinition txDef = new DefaultTransactionDefinition();
        TransactionStatus txStatus = mySqlAuthTransactionManager.getTransaction(txDef);
        User item;
        try {
            // Update the User.
            item = this.UserDao.changePassword(User, loggedInUser);
            this.printObjectService.PrintObject("UserService -> changePassword: User", item);

            // If everything was successful, commit to the database.
            mySqlAuthTransactionManager.commit(txStatus);
            this.logger.debug("UserService -> changePassword -> response: " + item);
        } catch (Exception e) {
            mySqlAuthTransactionManager.rollback(txStatus);
            logger.error("Error during update: " + User.getUsername(), e);
            throw e;
        }
        return item; // Note: this User does not include the updates
    }

    public String getUserGuidByUsername(String username) {
        this.logger.debug("UserService -> getUserGuidByUsername: username=" + username);
        return this.UserDao.getUserGuidByUsername(username);
    }

    public List<Role> getUserRoles(String userGuid) {
        this.logger.debug("UserService -> getUserRoles: userGuid=" + userGuid);
        return this.UserDao.getUserRoles(userGuid);
    }

}