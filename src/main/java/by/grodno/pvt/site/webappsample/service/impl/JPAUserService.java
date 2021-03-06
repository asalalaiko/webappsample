package by.grodno.pvt.site.webappsample.service.impl;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import by.grodno.pvt.site.webappsample.domain.User;
import by.grodno.pvt.site.webappsample.domain.UserCredentials;
import by.grodno.pvt.site.webappsample.domain.UserRole;
import by.grodno.pvt.site.webappsample.exception.UserNotFoundException;
import by.grodno.pvt.site.webappsample.repo.UserCredentialsRepo;
import by.grodno.pvt.site.webappsample.repo.UserRepo;
import by.grodno.pvt.site.webappsample.service.EmailService;
import by.grodno.pvt.site.webappsample.service.UserService;

@Service
@Transactional
public class JPAUserService implements UserService, InitializingBean {

	@Autowired
	private UserRepo repo;
	@Autowired
	private UserCredentialsRepo credRepo;
	@Autowired
	private EmailService emailService;

	@Override
	public void addUser(List<User> users) {
		repo.saveAll(users);
	}

	@Override
	public List<User> getUsers() {
		return repo.findAll();
	}

	@Override
	public void deleteUser(Integer number) {
		repo.deleteById(number);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		User oldUser = new User(null, "Maxim", "Naumovich", "max@max.max", null, UserRole.ADMIN, null, new Date());
		UserCredentials userCredentials = new UserCredentials(null,  new Date(), true, "max");
		oldUser.setCredentials(Collections.singletonList(userCredentials));
		repo.save(oldUser);
	}

	@Override
	public User getUser(Integer id) {
		return repo.getOne(id);
	}

	@Override
	public void saveUser(User user) {
		repo.save(user);
		emailService.sendUserActivationEmail(user);
	}

	@Override
	public Optional<User> findByEmail(String email) {
		return Optional.ofNullable(repo.findByEmail(email));
	}

	@Override
	public void activateUser(Integer id) {
		Optional<User> findById = repo.findById(id);

		findById.map(user -> {
			UserCredentials next = user.getCredentials().iterator().next();
			next.setActive(true);
			credRepo.save(next);
			return user;
		}).orElseThrow(() -> new UserNotFoundException());

	}

}
