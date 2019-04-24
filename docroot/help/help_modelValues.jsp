    <h2>Model Values</h2>

    <ul class="concise">
      <li><a href="#overview">Overview</a></li>
      <li><a href="#values">Values</a></li>
      <li><a href="#equation">Model Equation</a></li>
    </ul>
    
    <h3 id="overview">Overview</h3>
    <p>The <strong>Model Values</strong> report provides details on the results of the <strong>AFM (Additive
    Factor Model)</strong> algorithm<sup><a href="#1">1</a></sup>, a logistic regression performed over the
    &ldquo;error rate&rdquo; learning curve data. The AFM logistic regression, a standard regression
    bounded between 0 and 1, attempts to find the best-fit curve for error-rate data, which also ranges
    between 0 and 1.</p>
    
    <p>The Model Values page presents a quantitative analysis of how well, given the selected knowledge
    component model, the AFM statistical model fits the data (via AIC, BIC, log likelihood) and how
    well it might generalize to an independent dataset from the same tutor (via cross validation RMSE).</p>

    <!-- Note: If changing the following, also update the text in help_rsoftware.jsp -->    
    <p>Using R notation, the AFM model (applied to a modified student-step export file called "ds") 
    can be approximately* represented as:</p>
    <pre style="margin-left:2em">R&gt; L = length(ds$Anon.Student.Id)</pre>
    <pre style="margin-left:2em">R&gt; success = vector(mode="numeric", length=L)</pre>
    <pre style="margin-left:2em">R&gt; success[ds$First.Attempt=="correct"]=1</pre>
    <pre style="margin-left:2em">R&gt; model1.lmer &lt;- lmer(success~knowledge_component+
   knowledge_component:opportunity+(1|anon_student_id),data=ds,family=binomial())</pre>
   
    <p><strong>Note:</strong> The <code>success</code> variable must be 0 or 1. The first three R commands simply convert the "First Attempt" values (in the student-step export) of "incorrect" and "hint" to 0, and "correct" to "1".</p>
   
    <p><strong>*</strong> The AFM code is different from the R expression above in two ways:</p>
    <ol><li>To reduce over-fitting the data, AFM assumes learning cannot be negative and thus constraints the 
    "slope" estimates of the <code>knowledge_component:opportunity</code> parameters to be greater or equal to 0.<br />
    </li><li>The optimization applies a penalty to estimates of the student parameters (<code>anon_student_id</code>)
    for deviating from 0&mdash;essentially treating <code>anon_student_id</code> as a random effect.</li></ol>
    
    <p>AFM is a generalization of the log-linear test model (LLTM)<sup><a href="#2">2</a></sup>. 
    It is a specific instance of logistic regression, with student-success (0 or 1) as the dependent 
    variable and with independent variable terms for the student, the KC, and the KC by opportunity interaction. 
    Without the third term (KC by opportunity), AFM is LLTM. If the KC Model is the Unique-Step model 
    (and there is no third term), the model is Item Response Theory, or the Rasch model.</p>
    
    <p><a name="multi_skill_step_compensatory_sum">For KC models that contain steps coded with multiple skills</a>, 
    Datashop implements a compensatory sum across 
    all of the KCs of the multi-skilled steps, for both the KC intercept and slope. The sum is "conjunctive" when two (or more) 
    parameter estimates are negative, i.e. performance is predicted to be worse when the KCs are combined than 
    when they are alone. The sum is "disjunctive" when the parameter estimates are positive, i.e. performance 
    is predicted to be better when KCs are combined than when they are alone. If one estimate is positive and 
    one negative, the sum is "compensatory" like an average.
    </p>
    
    <p>AFM is run for all datasets in DataShop, once for each <a href="help?page=terms#kc_model"
    title="See the definition of 'knowledge component model'">knowledge component model</a> of a
    dataset. For large datasets, AFM will not run. "Large", in this case, is a function of the
    number of transactions, students, and KCs&mdash;a dataset with more than 300,000
    transactions, 250 students, and 300 KCs will prevent AFM from running successfully. The
    current workaround for this limitation is to create a smaller dataset with a subset of the
    data.</p>

    <h3 id="values">Values</h3>
    
    <p>Based on the knowledge component model and the observed data, the following AFM model values are 
    calculated:</p>
    
    <h4>KC Model Values</h4>
    <ul class="concise">
        <li><strong>AIC</strong>: The <em>Akaike information criterion</em> (AIC) is a measure of
        the goodness of fit of a statistical model, in this case, the AFM model. It is an
        operational way of trading off the complexity of the estimated model against how well the
        model fits the data<sup><a href="#2">3</a></sup>. In this way, it penalizes the model based
        on its complexity (the number of parameters). A lower AIC value is better.</li>
        <li><strong>BIC</strong>: The <em>Bayesian information criterion</em> (BIC) is also a 
        measure of goodness of fit of the AFM model. The BIC penalizes free parameters more strongly than does the 
        Akaike information criterion (AIC)<sup><a href="#3">4</a></sup>. A lower BIC value is 
        better.</li>
        <li><strong>Log Likelihood</strong>: a basic fit parameter used in calculating both AIC and BIC; 
        also referred to as the log likelihood ratio. Unlike AIC and BIC, log likelihood assumes
        the model includes the right number of parameters; AIC and BIC take into account that the
        parameters of the model could be wrong both in number and value.</li>
        <li><strong>Number of Parameters</strong>: the total number of parameters being fit by 
        the AFM statistical model. This number will vary with the number of knowledge components
        in the knowledge component model, as well as the number of students for which there is data
        in the dataset.</li>
        <li><strong>Number of Observations</strong>: the total number of observations used by 
        the AFM statistical model.</li>
    </ul>
    
    <h4>Cross Validation Values</h4>
    
    <p>Cross validation is a technique for assessing how well the results of a statistical model (in this case, AFM for
    a particular KC model) will generalize to an independent dataset from the same tutor. It's reported as <a
      href="http://en.wikipedia.org/wiki/Root_mean_squared_error">root mean squared error
    (RMSE)</a>. Lower values of RMSE indicate a better fit between the model's predictions and the
    observed data. </p>
     
    <p>Three types of cross validation are run for each KC model in the dataset. All types are a 
    <a href="http://en.wikipedia.org/wiki/Cross-validation_(statistics)#K-fold_cross-validation">3-fold
    cross validation</a> of the Additive Factor Model's (AFM) error rate predictions.</p>
        
     <ul class="concise">
        <li><strong>Student stratified.</strong> With data points grouped by student, the full set of students is
        divided into 3 groups. 3-fold cross validation is then performed across these 3 groups.</li>
        <li><strong>Item stratified.</strong> With data points grouped by step, the full set of steps is
        divided into 3 groups. 3-fold cross validation is then performed across these 3 groups.</li>
        <li><strong>Unstratified.</strong> The full set of data points is divided into 3 groups, irrespective of
        student or step. 3-fold cross validation is then performed across these 3 groups.
     </ul>

    <p>For item stratified cross validation, the system optimizes data division so that all KCs appear 
    in training sets at maximal possibility.</p> 
    
    <p>For unstratified cross validation, each student and each KC must appear in at least two observations; 
    otherwise that student or KC are excluded from cross validation. This procedure ensures that all students 
    and all KCs appear in both training and testing sets. However, the dropping of data points affects 
    the following two values:</p>
  
    <ul class="concise">
        <li><strong>Number of Parameters</strong>: the total number of parameters 
        used in cross validation. This number can differ from the number of parameters in the AFM model 
        if there are not enough observations for a student or KC. See the cross validation note above.</li>
        <li><strong>Number of Observations</strong>: the total number of observations 
        used in cross validation. This number can differ from the number of observations used by the AFM model 
        if there are not enough observations for a student or KC. See the unstratified cross validation note above.</li>
    </ul>

    <p>After taking into account the exclusion criteria described above, the following data requirements must be met 
    for cross validation to run:</p>
    
    <table style="margin-left: 1em; width: 355px">
    <tr>
        <th style="text-align:left">Cross Validation Type</th>
        <th style="text-align:left">Requirement</th>
    </tr>
    <tr>
        <td>Student stratified</td>
        <td>at least 3 students</td>
    </tr>
    <tr>
        <td>Item stratified</td>
        <td>at least 3 steps</td>
    </tr>
    <tr>
        <td>Unstratified</td>
        <td>at least 3 students and 3 KCs</td>
    </tr>
    </table>
    
    <h4>KC Values</h4>
    <ul class="concise">
        <li><strong>Intercept (logit)</strong>: a parameter representing knowledge component difficulty, 
        where higher values indicate an easier knowledge component and lower values indicate a more difficult KC.
        </li>
        <li><strong>Intercept (probability)</strong>: derived from the intercept (logit). 
        A parameter representing knowledge component difficulty, where higher values indicate an easier knowledge 
        component and lower values indicate a more difficult KC.</li>
        <li><strong>Slope</strong>: a parameter representing of how quickly students will learn the knowledge 
        component. The larger the KC slope, the faster students learn the knowledge component. </li>
    </ul>
    
    <h4>Student Values</h4>
    <ul class="concise">
        <li><strong>Intercept</strong>: a parameter representing a student's initial 
        knowledge. The higher the student intercept, the more the student initially knew.</li>
    </ul>
    
    <h4>To view model values for a different knowledge component model:</h4>
    <ul class="concise">
        <li>Select a different knowledge component model from the drop-down menu under <strong>Knowledge
        Component Models</strong>. The Model Values report will update to show values for that model.
        </li>
    </ul>
    <p><strong>Tip:</strong> To learn how to define a new knowledge component model, 
    see <a href="help?page=kcm">KC Models</a>. 
    
    <h4>To export model values:</h4>
    <ul class="concise">
        <li>Click the button labeled <strong>Export Model Values</strong>. You will be prompted to 
        save the exported text file.</li>
    </ul>
    
    <h3 id="equation">Model Equation</h3>
    <p>Predicted success rate is the probability of the student being correct on the first try (no hint request 
    or incorrect action) on a step.</p>
    
    <div class="figure-center">
        <img src="images/help/inverse_logit_AFM_datashop.png" alt="Additive Factor Model (AFM)" 
            title="Additive Factor Model (AFM)" style="border:none" />
    </div>
    
    <p>where</p>
    
    <ul class="concise" style="list-style: none">
        <li>&Upsilon;<sub>ij</sub> = the response of student i on item j</li>
        <li>&theta;<sub>i</sub> = coefficient for proficiency of student i</li>
        <li>&beta;<sub>k</sub> = coefficient for difficulty of knowledge component k</li>
        <li>&gamma;<sub>k</sub> = coefficient for the learning rate of knowledge component k</li>
        <li>&Tau;<sub>ik</sub> = the number of practice opportunities student i has had on the knowledge component k</li>
    </ul>
    
    <p>and</p>
    
    <div class="figure-center">
        <img src="images/help/lfa-afm-1.png" alt="Additive Factor Model (AFM)" style="border:none" />
    </div>
    
    <ul class="concise" style="list-style: none">
        <li>&Kappa; = the total number of knowledge components in the Q-matrix</li>
    </ul>
    
    <p><strong>Note:</strong></p>
    <ul class="concise">
        <li>The &Tau;<sub>ik</sub> parameter estimate (the number of practice opportunities student i 
        has had on the knowledge component k) is constrained to be greater or equal to 0.</li>
        <li>User proficiency parameters (&theta;<sub>i</sub>) are fit using a Penalized Maximum 
        Likelihood Estimation method (PMLE) to overcome over fitting. User proficiencies are seeded 
        with normal priors and PMLE penalizes the oversized student parameters in the joint estimation 
        of the student and the skill parameters.</li>
    </ul>
    
    <p>The intuition of this model is that the probability of a student getting a step correct is
    proportional to the amount of required knowledge the student knows, plus the "easiness" of that
    knowledge component, plus the amount of learning gained for each practice opportunity.</p>
    
    <p>The term "Additive" comes from the fact that a linear combination of
    knowledge component parameters determines logit(<em>p<sub>ij</sub></em>) in the equation. For
    more information on the Additive Factor Model, see Hao Cen's PhD Thesis<sup><a href="#5">5</a></sup>.</p>
    
    <p class="footnote"><sup id="1">1</sup>
    Cen, H., Koedinger, K. R., and Junker, B. 2007. Is Over Practice Necessary? --Improving 
    Learning Efficiency with the Cognitive Tutor through Educational Data Mining. 
    In Proceeding of the 2007 Conference on Artificial intelligence in Education: 
    Building Technology Rich Learning Contexts that Work R. Luckin, K. R. Koedinger, 
    and J. Greer, Eds. Frontiers in Artificial Intelligence and Applications, vol. 158. 
    IOS Press, Amsterdam, The Netherlands, 511-518. 
    <a href="http://www.learnlab.org/uploads/mypslc/publications/lfa%20efficiency%20study%203.61%20use%20the%20official%20template.pdf">PDF</a>
    </p>
    
    <p class="footnote"><sup></sup> Cen, H., Koedinger, K., and Junker, B. 2008. Comparing Two IRT Models for Conjunctive Skills. 
    In Proceedings of the 9th international Conference on intelligent Tutoring Systems (Montreal, Canada, June 23 - 27, 2008). 
    B. P. Woolf, E. A&iuml;meur, R. Nkambou, and S. Lajoie, Eds. Lecture Notes In Computer Science. Springer-Verlag, Berlin, 
    Heidelberg, 796-798. DOI=<a href="http://dx.doi.org/10.1007/978-3-540-69132-7_111">http://dx.doi.org/10.1007/978-3-540-69132-7_111</a></p> 
    
    <p class="footnote"><sup id="2">2</sup> Wilson, M., & De Boeck, P. (2004). Descriptive and explanatory item response models. In P. De Boeck, & M. Wilson, (Eds.) 
    Explanatory item response models: A generalized linear and nonlinear approach. New York: Springer-Verlag. 
    </p>
    
    <p class="footnote"><sup id="3">3</sup> Akaike information criterion. (2007, February 25). In
    <i>Wikipedia, The Free Encyclopedia</i>. Retrieved 16:22, March 7, 2007, from
    <a href="http://en.wikipedia.org/w/index.php?title=Akaike_information_criterion&oldid=110898239">
    http://en.wikipedia.org/w/index.php?title=Akaike_information_criterion&amp;oldid=110898239</a>
    </p>
    
    <p class="footnote"><sup id="4">4</sup> Bayesian information criterion. (2007, February 19). In
    <i>Wikipedia, The Free Encyclopedia</i>. Retrieved 16:29, March 7, 2007, from <a
    href="http://en.wikipedia.org/w/index.php?title=Bayesian_information_criterion&amp;oldid=109323430">
    http://en.wikipedia.org/w/index.php?title=Bayesian_information_criterion&amp;oldid=109323430</a>
    </p>
    
    <p class="footnote"><sup id="5">5</sup> Cen, H. (2009). <em>Generalized Learning Factors Analysis: Improving Cognitive 
    Models with Machine Learning</em>. <a
    href="http://reports-archive.adm.cs.cmu.edu/anon/ml2009/CMU-ML-09-102.pdf">PDF</a></p>

