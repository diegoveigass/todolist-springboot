package com.diegoveiga.todolist.task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.diegoveiga.todolist.utils.Utils;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/tasks")
public class TaskController {
  @Autowired
  private ITaskRepository taskRepository;

  @PostMapping("/")
  public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest request) {

    taskModel.setUserId((UUID) request.getAttribute("userId"));

    var currentDate = LocalDateTime.now();

    if (currentDate.isAfter(taskModel.getStartAt()) || currentDate.isAfter(taskModel.getEndAt())) {
      return ResponseEntity.status(400).body("Time of start/end task is after of the current date");
    }

    if (taskModel.getEndAt().isBefore(taskModel.getStartAt())) {
      return ResponseEntity.status(400).body("Time of end task is before of the start date task");
    }


    var task = taskRepository.save(taskModel);

    return ResponseEntity.status(201).body(task);
  }

  @GetMapping("/")
  public List<TaskModel> getlist(HttpServletRequest request) {
    var tasks = taskRepository.findByUserId((UUID) request.getAttribute("userId"));

    return tasks;
  }

  @PutMapping("/{id}")
  public ResponseEntity update(@RequestBody TaskModel taskModel, @PathVariable UUID id, HttpServletRequest request) {
    var userId = (UUID) request.getAttribute("userId");

    var task = taskRepository.findById(id).orElse(null);

    if (task == null) {
      return ResponseEntity.status(400).body("Task not exists");
    }

    Utils.copyNonNullProperties(taskModel, task);


    if (!task.getUserId().equals(userId)) {
      return ResponseEntity.status(400).body("You are not owner of this task");
    }

    var taskUpdated = taskRepository.save(task);

    return ResponseEntity.status(200).body(taskUpdated);
  }
}
