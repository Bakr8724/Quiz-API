package com.cooksys.quiz_api.services.impl;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.cooksys.quiz_api.dtos.QuestionRequestDto;
import com.cooksys.quiz_api.dtos.QuestionResponseDto;
import com.cooksys.quiz_api.dtos.QuizRequestDto;
import com.cooksys.quiz_api.dtos.QuizResponseDto;
import com.cooksys.quiz_api.entities.Question;
import com.cooksys.quiz_api.entities.Quiz;
import com.cooksys.quiz_api.mappers.QuestionMapper;
import com.cooksys.quiz_api.mappers.QuizMapper;
import com.cooksys.quiz_api.repositories.QuestionRepository;
import com.cooksys.quiz_api.repositories.QuizRepository;
import com.cooksys.quiz_api.services.QuizService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

  private final QuizRepository quizRepository;
  private final QuizMapper quizMapper;
  private final QuestionMapper questionMapper;
  private final QuestionRepository questionRepository;


  private Quiz getQuizById(Long id){
    Optional<Quiz> optionalQuiz = quizRepository.findById(id);
    if(optionalQuiz.isEmpty()){
      throw new RuntimeException("No quiz found with id: " + id);
    }
    return optionalQuiz.get();
  }

  @Override
  public List<QuizResponseDto> getAllQuizzes() {
    return quizMapper.entitiesToDtos(quizRepository.findAll());
  }

  @Override
  public QuizResponseDto createQuiz(QuizRequestDto quizRequestDto) {
    Quiz quiz = quizMapper.dtoToEntity(quizRequestDto);

    quiz = quizRepository.save(quiz);

    return quizMapper.entityToDto(quiz);
  }


  @Override
  public QuizResponseDto deleteQuiz(Long id) {
      Quiz quiz = quizRepository.findById(id).orElseThrow(() -> new RuntimeException("Quiz not found"));
      
      quizRepository.delete(quiz);
      
      return quizMapper.entityToDto(quiz);
  }

  @Override
  public QuizResponseDto renameQuiz(Long id, String newName) {
    Quiz quizToRename = getQuizById(id);

    quizToRename.setName(newName);
    
    quizRepository.saveAndFlush(quizToRename);
    
    return quizMapper.entityToDto(quizToRename);
  }

  @Override
  public QuestionResponseDto getRandomQuestion(Long id) {
    Quiz quiz = getQuizById(id);
    List<Question> questions = quiz.getQuestions();

    if(questions.isEmpty()){
      throw new RuntimeException("No question found: " + id);
    }

    Random random = new Random();
    Question randomQuestion = questions.get(random.nextInt(questions.size()));
    return questionMapper.entityToDto(randomQuestion);
  }


  @Override
  public QuizResponseDto addQuestionToQuiz(Long id, QuestionRequestDto questionRequestDto) {
      Quiz quiz = getQuizById(id);
      
      Question newQuestion = questionMapper.requestDtoToEntity(questionRequestDto);
      newQuestion.setQuiz(quiz);

      questionRepository.saveAndFlush(newQuestion);

      quiz.getQuestions().add(newQuestion);
      
      return quizMapper.entityToDto(quiz);
  }

  @Override
  public QuestionResponseDto deleteQuestion(Long id, Long questionId) {
    Quiz quiz = getQuizById(id);

    Question questionToDelete = questionRepository.findById(questionId).orElseThrow(() -> new RuntimeException("no question found: " + questionId));
    
    if(!quiz.getQuestions().contains(questionToDelete)){
      throw new RuntimeException("The question :" + questionId + "is not in the specified quiz: " + id);
    }

    quiz.getQuestions().remove(questionToDelete);
    questionRepository.delete(questionToDelete);

    return questionMapper.entityToDto(questionToDelete);
  }


}
