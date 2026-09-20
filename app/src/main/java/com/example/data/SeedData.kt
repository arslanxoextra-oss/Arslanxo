package com.example.data

import com.example.model.AppPolicies
import com.example.model.ImagePost
import com.example.model.Post

object SeedData {
    val defaultPolicies = AppPolicies(
        id = "default_policy",
        whatsappchannel = "https://whatsapp.com/channel/0029Vaexample",
        rateus = "https://play.google.com/store/apps/details?id=com.arslanaziz.promptxo",
        privatepolicies = "https://sites.google.com/view/promptxo-privacy-policy"
    )

    val initialPosts: List<Post> = listOf(
        Post(
            id = "post_1",
            title = "Kids Educational",
            description = "Create unique 8-second 3D educational videos for kids.",
            category = "Cartoon",
            homeCategory = "Cartoon",
            StepsNumbers = "1 Step",
            step1title = "Generate Video Prompt",
            step1description = "Click Generate Prompt to create a video prompt.",
            step1prompt = "Create a 3D Pixar-style educational animation of a cute curious boy sitting at a desk full of colorful storybooks, soft warm morning lighting, highly detailed 8k, cinematic camera orbit.",
            step1toolname = "Try in ChatGPT",
            step1toollink = "https://chatgpt.com",
            has2bean = false,
            thumbnail = "https://images.unsplash.com/photo-1503676260728-1c00da094a0b?w=600&auto=format&fit=crop&q=80",
            createdAt = System.currentTimeMillis()
        ),
        Post(
            id = "post_2",
            title = "Cinematic Story",
            description = "Create a cinematic short video with emotional storytelling.",
            category = "Faceless",
            homeCategory = "Faceless",
            StepsNumbers = "2 Steps",
            step1title = "Script Outline & Hook",
            step1description = "Generate dramatic narrative hook and visual beats.",
            step1prompt = "Generate a dramatic 60-second video script about a lonely traveler standing in the pouring rain in front of a warm lit cottage in the Swiss Alps, cinematic voiceover hook.",
            step1toolname = "Try in ChatGPT",
            step1toollink = "https://chatgpt.com",
            step2title = "Visual Prompts for Runway",
            step2description = "Create video motion prompts for AI video generator.",
            step2prompt = "Cinematic medium wide shot, dramatic sunset over snowy pine trees, golden light reflection on wet stones, slow motion 4k 60fps.",
            step2toolname = "Try in Runway",
            step2toollink = "https://runwayml.com",
            has2bean = true,
            thumbnail = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600&auto=format&fit=crop&q=80",
            createdAt = System.currentTimeMillis() - 3600000L
        ),
        Post(
            id = "post_3",
            title = "Cartoon Character",
            description = "Create a 3D cartoon character with consistent style.",
            category = "Cartoon",
            homeCategory = "Cartoon",
            StepsNumbers = "2 Steps",
            step1title = "Character Design Sheet",
            step1description = "Generate multi-angle character concept.",
            step1prompt = "Cute robotic character with glowing blue optical eyes, metallic white armor, adorable friendly expression, front, side, and 3/4 view, clean gradient studio background.",
            step1toolname = "Try in ChatGPT",
            step1toollink = "https://chatgpt.com",
            step2title = "Animation Sequence Prompt",
            step2description = "Generate actions and expressions.",
            step2prompt = "The cute white robot waves happily with its mechanical hand, floating slightly above the floor with glowing thrusters, smooth motion.",
            step2toolname = "Try in Midjourney",
            step2toollink = "https://midjourney.com",
            has2bean = true,
            thumbnail = "https://images.unsplash.com/photo-1485827404703-89b55fcc595e?w=600&auto=format&fit=crop&q=80"
        ),
        Post(
            id = "post_4",
            title = "Wealth & Crypto Secrets",
            description = "Create engaging faceless finance videos for YouTube and TikTok.",
            category = "Finance",
            homeCategory = "Finance",
            StepsNumbers = "1 Step",
            step1title = "Financial Script Hook",
            step1description = "Generate viral financial psychology hooks.",
            step1prompt = "Write an eye-opening script explaining the compound interest secret used by billionaires, with bold kinetic typography cues and dramatic sound design beats.",
            step1toolname = "Try in ChatGPT",
            step1toollink = "https://chatgpt.com",
            has2bean = false,
            thumbnail = "https://images.unsplash.com/photo-1611974789855-9c2a0a7236a3?w=600&auto=format&fit=crop&q=80"
        ),
        Post(
            id = "post_5",
            title = "Ancient Rome Origins",
            description = "High-production historical documentary video prompts.",
            category = "Documentary",
            homeCategory = "Documentary",
            StepsNumbers = "2 Steps",
            step1title = "Documentary Narration",
            step1description = "Historical narration with grave BBC style tone.",
            step1prompt = "Narrate the fall of Julius Caesar with profound historical gravity, setting the scene in the rain-drenched streets of Rome, March 44 BC.",
            step1toolname = "Try in ChatGPT",
            step1toollink = "https://chatgpt.com",
            step2title = "Historical Visual Reconstruction",
            step2description = "Photorealistic ancient architecture prompts.",
            step2prompt = "Photorealistic recreation of the Roman Senate with marble pillars, senators in authentic wool togas, dramatic candle and torch lighting.",
            step2toolname = "Try in Leonardo",
            step2toollink = "https://leonardo.ai",
            has2bean = true,
            thumbnail = "https://images.unsplash.com/photo-1552832230-c0197dd311b5?w=600&auto=format&fit=crop&q=80"
        ),
        Post(
            id = "post_6",
            title = "Nature Wolf",
            description = "Majestic arctic wolf in snowy winter mountain forest.",
            category = "Documentary",
            homeCategory = "Documentary",
            StepsNumbers = "1 Step",
            step1title = "Wildlife Shot",
            step1description = "National Geographic style animal photography.",
            step1prompt = "Close-up portrait of a majestic white wolf with piercing amber eyes, frost on fur, falling snowflakes, soft bokeh pine tree background, high shutter speed, 8k.",
            step1toolname = "Try in ChatGPT",
            step1toollink = "https://chatgpt.com",
            has2bean = false,
            thumbnail = "https://images.unsplash.com/photo-1564865878688-9a244444042a?w=600&auto=format&fit=crop&q=80"
        ),
        Post(
            id = "post_7",
            title = "Stock Market Crash",
            description = "Fast-paced market analysis and trading psychology scripts.",
            category = "Finance",
            homeCategory = "Finance",
            StepsNumbers = "2 Steps",
            step1title = "Market Breakdown Script",
            step1description = "Viral finance hook analyzing market trends.",
            step1prompt = "Generate a gripping 30-second explanation of inflation and hedge fund maneuvers, with punchy storytelling and visual chart references.",
            step1toolname = "Try in ChatGPT",
            step1toollink = "https://chatgpt.com",
            step2title = "Cyber Wall Street Visuals",
            step2description = "Neon financial graphs and trading desks.",
            step2prompt = "Cinematic dark room with multiple glowing green and red candlestick stock charts, silhouette of analyst looking at market data.",
            step2toolname = "Try in Midjourney",
            step2toollink = "https://midjourney.com",
            has2bean = true,
            thumbnail = "https://images.unsplash.com/photo-1590283603385-17ffb3a7f29f?w=600&auto=format&fit=crop&q=80"
        ),
        Post(
            id = "post_8",
            title = "Deep Ocean Mysteries",
            description = "Explore undiscovered ocean trenches and marine creatures.",
            category = "Documentary",
            homeCategory = "Documentary",
            StepsNumbers = "1 Step",
            step1title = "Abyssal Exploration",
            step1description = "BBC Blue Planet style exploration script.",
            step1prompt = "Underwater submersible headlights cutting through pitch black Mariana Trench, revealing bioluminescent giant squid, cinematic slow panning shot.",
            step1toolname = "Try in ChatGPT",
            step1toollink = "https://chatgpt.com",
            has2bean = false,
            thumbnail = "https://images.unsplash.com/photo-1544551763-46a013bb70d5?w=600&auto=format&fit=crop&q=80"
        )
    )

    val initialImagePosts: List<ImagePost> = listOf(
        ImagePost(
            id = "img_1",
            imagetitle = "Cyberpunk Samurai",
            imageprompt = "Futuristic neon cyberpunk samurai standing in rain drenched Neo Tokyo alley, glowing katana blade, volumetric fog, hyper realistic 8k.",
            imagetoolname = "Try in Midjourney",
            imagetoollink = "https://midjourney.com",
            thumbnail = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=600&auto=format&fit=crop&q=80",
            categoryforall = "All",
            categoryfornew = "Trending",
            categoryfortrending = "Portrait",
            createdAt = System.currentTimeMillis()
        ),
        ImagePost(
            id = "img_2",
            imagetitle = "Nature Wolf",
            imageprompt = "A majestic snow wolf sitting atop a rocky cliff in blizzard, intense amber gaze, intricate fur texture, 85mm lens f/1.4.",
            imagetoolname = "Try in Midjourney",
            imagetoollink = "https://midjourney.com",
            thumbnail = "https://images.unsplash.com/photo-1564865878688-9a244444042a?w=600&auto=format&fit=crop&q=80",
            categoryforall = "All",
            categoryfornew = "New",
            categoryfortrending = "Portrait",
            createdAt = System.currentTimeMillis() - 3600000L
        ),
        ImagePost(
            id = "img_3",
            imagetitle = "Warrior in Red",
            imageprompt = "Mystical female warrior wearing a crimson hooded cloak in an autumn misty forest, glowing magic runes floating in the air.",
            imagetoolname = "Try in Leonardo",
            imagetoollink = "https://leonardo.ai",
            thumbnail = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=600&auto=format&fit=crop&q=80",
            categoryforall = "All",
            categoryfornew = "Portrait",
            categoryfortrending = "Trending"
        ),
        ImagePost(
            id = "img_4",
            imagetitle = "Futuristic Hypercar",
            imageprompt = "Sleek aerodynamic electric hypercar speeding across desert salt flats at dusk, orange afterglow sky, motion blur on wheels.",
            imagetoolname = "Try in ChatGPT",
            imagetoollink = "https://chatgpt.com",
            thumbnail = "https://images.unsplash.com/photo-1617814076367-b759c7d7e738?w=600&auto=format&fit=crop&q=80",
            categoryforall = "All",
            categoryfornew = "Video",
            categoryfortrending = "Trending"
        ),
        ImagePost(
            id = "img_5",
            imagetitle = "Neon Cyber City",
            imageprompt = "Futuristic purple and cyan neon electric metropolis at midnight, flying vehicles leaving light trails between skyscrapers.",
            imagetoolname = "Try in Midjourney",
            imagetoollink = "https://midjourney.com",
            thumbnail = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=600&auto=format&fit=crop&q=80",
            categoryforall = "All",
            categoryfornew = "Video",
            categoryfortrending = "Trending"
        ),
        ImagePost(
            id = "img_6",
            imagetitle = "Golden Temple",
            imageprompt = "Ancient oriental pagoda on a mountain summit glowing during golden sunset, cherry blossoms floating in breeze.",
            imagetoolname = "Try in ChatGPT",
            imagetoollink = "https://chatgpt.com",
            thumbnail = "https://images.unsplash.com/photo-1493976040374-85c8e12f0c0e?w=600&auto=format&fit=crop&q=80",
            categoryforall = "All",
            categoryfornew = "New",
            categoryfortrending = "Trending"
        ),
        ImagePost(
            id = "img_7",
            imagetitle = "Cyberpunk Android",
            imageprompt = "Close up studio portrait of an elegant female android with glowing porcelain skin, golden wiring beneath temple, studio rim light.",
            imagetoolname = "Try in Midjourney",
            imagetoollink = "https://midjourney.com",
            thumbnail = "https://images.unsplash.com/photo-1531746020798-e6953c6e8e04?w=600&auto=format&fit=crop&q=80",
            categoryforall = "All",
            categoryfornew = "Portrait",
            categoryfortrending = "Trending"
        ),
        ImagePost(
            id = "img_8",
            imagetitle = "Cosmic Explorer",
            imageprompt = "Astronaut in sleek titanium spacesuit standing on an alien planet with two moons in sky, crystalline formations.",
            imagetoolname = "Try in Leonardo",
            imagetoollink = "https://leonardo.ai",
            thumbnail = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=600&auto=format&fit=crop&q=80",
            categoryforall = "All",
            categoryfornew = "Video",
            categoryfortrending = "New"
        ),
        ImagePost(
            id = "img_9",
            imagetitle = "Cinematic Action Chase",
            imageprompt = "Explosive cinematic car chase through rain swept city bridge, shattered glass, dynamic camera angle, 35mm film grain.",
            imagetoolname = "Try in Runway",
            imagetoollink = "https://runwayml.com",
            thumbnail = "https://images.unsplash.com/photo-1492144534655-ae79c964c9d7?w=600&auto=format&fit=crop&q=80",
            categoryforall = "All",
            categoryfornew = "Video",
            categoryfortrending = "Trending"
        ),
        ImagePost(
            id = "img_10",
            imagetitle = "Fantasy Queen",
            imageprompt = "Royal portrait of an elven queen with an emerald crown, soft flowing silvery hair, ethereal magical glow, oil painting texture.",
            imagetoolname = "Try in Midjourney",
            imagetoollink = "https://midjourney.com",
            thumbnail = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=600&auto=format&fit=crop&q=80",
            categoryforall = "All",
            categoryfornew = "Portrait",
            categoryfortrending = "New"
        )
    )
}
