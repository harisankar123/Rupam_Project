package com.hari.demo;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
//import java.util.Base64.Decoder;
import java.util.Base64.Decoder;

import java.util.Base64;
import javax.servlet.http.HttpServletRequest;

import org.hibernate.persister.entity.Queryable.Declarer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import com.hari.demo.Post;
import com.hari.demo.PostRepository;


 
@Controller
public class PostManager {
	@Autowired
	PostRepository pRepo;
	 @Autowired
	 UploadToS3 s3;
	@GetMapping(value="/recordAudio")
	public ModelAndView renderIndex() {
		return new ModelAndView("recordAudio");
	}
	@PostMapping(value="/base64Audio")
	public ModelAndView saveAudioandPostPage(@RequestParam("recording") String recording ,
			@RequestParam("file") MultipartFile image,
			HttpServletRequest request) throws Exception {
		
		ModelAndView mv = new ModelAndView();
		System.out.println("incoming Message");
		if(recording.isEmpty()) throw new Exception ("recording data is null");
		Decoder decoder= Base64.getDecoder();
		System.out.println("recording");
		byte[] decodedByte= decoder.decode(recording.split(",")[1]);
		FileOutputStream fos;
		try {
			fos = new FileOutputStream("MyAudio.webm");
			fos.write(decodedByte);
			fos.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		Post post = new Post();
		String myId= (String) request.getSession().getAttribute("userId");
		//System.out.println(myId);
		String postId= "1";
		String audioUrl=s3.upload(myId+postId+"webm",new FileInputStream("MyAudio.webm"));
		String addr = s3.upload(image.getOriginalFilename(),image.getInputStream());
		System.out.println(addr);
		System.out.println("audioUrl");
		post.setUserId(myId);
		post.setPostPhoto(addr);
		post.setPostAudio(audioUrl);
		pRepo.save(post);
		System.out.println("post save");
		
		//mv.addObject("post",post);
		mv.setViewName("success");
		return mv;
		
	}
	@PostMapping(value="/profileRedirect")
	public ModelAndView savePostPorfile(@RequestParam("file") MultipartFile image,
			@RequestParam("audioUrl") String audioUrl,HttpServletRequest req) {
		ModelAndView mv = new ModelAndView();
		String myId= (String)req.getSession().getAttribute("userId");
		Post post= pRepo.findByUserId(myId);
		mv.addObject("post",post);
		System.out.println("**********************"+post.getUserId());
		return mv;
		
	}
	
	
}
 